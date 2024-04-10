import ray

from model.Artifact import Artifact
from nodes.DataNode import DataNode
from nodes.NameNode import NameNode


@ray.remote
class ClientNode:
    def __init__(self, name_node: NameNode, data_nodes: list[DataNode]) -> None:
        self.__name_node = name_node
        self.__data_nodes = data_nodes

    def read(self, artifact_name: str) -> tuple[Artifact, int, str]:
        """Returns: `(artifact: Artifact, node_id: int, error: str)`"""
        does_artifact_exist = ray.get(
            self.__name_node.does_artifact_exist.remote(artifact_name)
        )
        if not does_artifact_exist:
            error = f"Artifact named '{artifact_name}' doesn't exist"
            return None, None, error

        data_nodes = ray.get(
            self.__name_node.get_artifact_data_nodes.remote(artifact_name)
        )
        if len(data_nodes) == 0:
            error = f"Internal error: Artifact '{artifact.name}' exists in NameNode, but is not stored in any DataNode"
            return error

        all_ray_ids = [node.get_artifact.remote(artifact_name) for node in data_nodes]
        while len(all_ray_ids) > 0:
            [ready_ray_id], reamining_ray_ids = ray.wait(all_ray_ids, num_returns=1)
            ready_index = all_ray_ids.index(ready_ray_id)

            ready_node_id = ray.get(data_nodes[ready_index].get_id.remote())
            artifact = ray.get(ready_ray_id)

            if artifact is not None:
                for ray_id in reamining_ray_ids:
                    ray.cancel(ray_id)
                return artifact, ready_node_id, None
            else:
                print(
                    f"Warning: DataNode#{ready_node_id} failed to return content of artifact '{artifact_name}'"
                )
                all_ray_ids.pop(ready_index)

        all_node_ids = sorted(ray.get([node.get_id.remote() for node in data_nodes]))
        error = f"All DataNodes failed to return content of artifact '{artifact_name}': " + f"{[f'DataNode#{id}' for id in all_node_ids]}"
        
        return None, None, error

    def save(self, artifact: Artifact) -> str | None:
        """Returns error message on failure"""
        already_exists = ray.get(
            self.__name_node.does_artifact_exist.remote(artifact.name)
        )
        if already_exists:
            error = f"Artifact named '{artifact.name}' already exists"
            return error

        self.__name_node.save_artifact.remote(artifact)
        return None

    def modify(self, artifact: Artifact) -> str | None:
        """Returns error message on failure"""
        does_artifact_exist = ray.get(
            self.__name_node.does_artifact_exist.remote(artifact.name)
        )
        if not does_artifact_exist:
            error = f"Artifact named '{artifact.name}' doesn't exist"
            return error

        data_nodes = ray.get(
            self.__name_node.get_artifact_data_nodes.remote(artifact.name)
        )
        for data_node in data_nodes:
            data_node.save_or_update_artifact.remote(artifact)

        return None

    def delete(self, artifact_name: str) -> str | None:
        """Returns error message on failure"""
        does_artifact_exist = ray.get(
            self.__name_node.does_artifact_exist.remote(artifact_name)
        )
        if not does_artifact_exist:
            error = f"Artifact named '{artifact_name}' doesn't exist"
            return error

        self.__name_node.delete_artifact.remote(artifact_name)
        return None

    def get_status(self) -> tuple[tuple[int, list[str]], list[tuple[int, list[str]]]]:
        """Returns: `(name_node_status, data_nodes_statuses)`.
        `name_node_status -> (data_nodes_number: int, saved_artifact_names: list[str])`
        `data_nodes_statuses -> list[(node_id: int, saved_artifact_names: list[str])]`
        """

        name_node_status_ray_id = self.__name_node.get_status.remote()
        data_nodes_statuses_ray_ids = [
            data_node.get_status.remote() for data_node in self.__data_nodes
        ]

        name_node_status = ray.get(name_node_status_ray_id)
        data_nodes_statuses = ray.get(data_nodes_statuses_ray_ids)

        return name_node_status, data_nodes_statuses
