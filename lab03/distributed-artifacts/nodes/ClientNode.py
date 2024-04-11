import ray

from model.Artifact import Artifact, ArtifactSegment
from nodes.DataNode import DataNode
from nodes.NameNode import NameNode


@ray.remote
class ClientNode:
    def __init__(self, name_node: NameNode, data_nodes: list[DataNode]) -> None:
        self.__name_node = name_node
        self.__data_nodes = data_nodes
    
    def __schedule_single_segments_read(cls, data_nodes_per_segment: list[list[DataNode]], artifact_name: str) -> tuple[list[list], str]:
        """Returns: `(scheduled_ray_ids_per_segment: list[list]), error: str`"""

        scheduled_ray_ids_per_segment = []
        for segment_index, data_nodes in enumerate(data_nodes_per_segment):
            if len(data_nodes) == 0:
                error = f"Internal error: Segment {segment_index} of artifact '{artifact_name}' exists in NameNode, but is not stored in any DataNode"

                for id in [id for ids in scheduled_ray_ids_per_segment for id in ids]:
                    ray.cancel(id)

                return None, error
        
            scheduled_ray_ids = [node.get_artifact_segment.remote(artifact_name) for node in data_nodes]
            scheduled_ray_ids_per_segment.append(scheduled_ray_ids)

        return scheduled_ray_ids_per_segment, None
    
    def __get_segment_from_scheduled(cls, ray_ids: list[list], data_nodes: list[DataNode]) -> tuple[ArtifactSegment, int, list[int]]:
        """Returns: `(artifact_segment: ArtifactSegment, node_id: int, failed_nodes_ids: list[int])`"""

        all_ray_ids = ray_ids.copy()
        reamining_data_nodes = data_nodes.copy()

        failed_nodes_ids = []
        while len(all_ray_ids) > 0:
            [ready_ray_id], reamining_ray_ids = ray.wait(all_ray_ids, num_returns=1)
            ready_index = all_ray_ids.index(ready_ray_id)

            ready_node_id = ray.get(reamining_data_nodes[ready_index].get_id.remote())
            artifact_segment = ray.get(ready_ray_id)

            if artifact_segment is not None:
                for ray_id in reamining_ray_ids:
                    ray.cancel(ray_id)
                return artifact_segment, ready_node_id, failed_nodes_ids
            
            else:
                failed_nodes_ids.append(ready_node_id)
                all_ray_ids.pop(ready_index)
        
        return None, None, failed_nodes_ids

    # def __get_one_response_per_segment(ray_ids: list[list], data_nodes_per_segment: list[list[DataNode]]):
    #     ray_ids = [sublist.copy() for sublist in ray_ids]
    #     ready_ids = []    # list[ray_id]
    #     remainig_ids = [] # list[list[ray_id]]

    #     # for segment_index, segment_ids in enumerate(ray_ids):
    #     # for segment_index, segment_ids in enumerate(ray_ids):
    #     for segment_index in range(len(ray_ids)):

    #         all_segment_ray_ids = ray_ids[segment_index]
    #         # [ready_ray_id], reamining_ray_ids = ray.wait(segment_ids, num_returns=1)
    #         # returned_segment = ray.get(ready_ray_id)
    #         # ready_ids.append()

    def read(self, artifact_name: str) -> tuple[Artifact, list[int], str, str]:
        """Returns: `(artifact: Artifact, used_nodes_ids: list[int], error: str, warning: str)`"""
        does_artifact_exist = ray.get(
            self.__name_node.does_artifact_exist.remote(artifact_name)
        )
        if not does_artifact_exist:
            error = f"Artifact named '{artifact_name}' doesn't exist"
            return None, None, error, None

        data_nodes_per_segment = ray.get(
            self.__name_node.get_artifact_data_nodes.remote(artifact_name)
        )
        scheduled_ray_ids_per_segment, error = self.__schedule_single_segments_read(data_nodes_per_segment, artifact_name)
        if error is not None:
            return None, None, error, None
        
        warning = ""
        read_segments = []
        used_nodes_ids = []
        for segment_index, (data_nodes, ray_ids) in enumerate(zip(data_nodes_per_segment, scheduled_ray_ids_per_segment)):
            artifact_segment, node_id, failed_nodes_ids = self.__get_segment_from_scheduled(ray_ids, data_nodes)

            failed_nodes_str = f"{[f'DataNode#{id}' for id in failed_nodes_ids]}"
            if node_id is None:
                error = f"All DataNodes failed to return segment {segment_index} of  artifact '{artifact_name}': " + failed_nodes_str
        
                return None, None, error, None
            
            if len(failed_nodes_ids) > 0:
                warning += f"Some nodes failed to return segment {segment_index} of  artifact '{artifact_name}': " + failed_nodes_str
            
            read_segments.append(artifact_segment)
            used_nodes_ids.append(node_id)

        artifact = Artifact.recreate_from_segments(read_segments)
        return artifact, used_nodes_ids, None, None

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

        data_nodes_per_segment = ray.get(
            self.__name_node.get_artifact_data_nodes.remote(artifact.name)
        )
        segments = artifact.split_into_parts(segments_number=len(data_nodes_per_segment))

        for segment, data_nodes in zip(segments, data_nodes_per_segment):
            for node in data_nodes:
                node.save_or_update_segment.remote(segment)

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

    def get_status(self) -> tuple[tuple[int, list[str]], list[tuple[int, list[tuple[str, int]]]]]:
        """Returns: `(name_node_status, data_nodes_statuses)`.
        `name_node_status -> (data_nodes_number: int, saved_artifact_names: list[str])`
        `data_nodes_statuses -> list[(node_id: int, saved_segments_names_and_indexes: list[tuple[str, int]])]`
        """

        name_node_status_ray_id = self.__name_node.get_status.remote()
        data_nodes_statuses_ray_ids = [
            data_node.get_status.remote() for data_node in self.__data_nodes
        ]

        name_node_status = ray.get(name_node_status_ray_id)
        data_nodes_statuses = ray.get(data_nodes_statuses_ray_ids)

        return name_node_status, data_nodes_statuses
