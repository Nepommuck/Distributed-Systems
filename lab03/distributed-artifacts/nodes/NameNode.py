import ray
import random

from model.Artifact import Artifact
from nodes.DataNode import DataNode


@ray.remote
class NameNode:
    def __init__(self, data_nodes: list[DataNode], replica_count: int = 4) -> None:
        self.__replica_count = replica_count
        # existing_artifacts: `artifact_name: str -> data_nodes: list[DataNode]`
        self.__existing_artifacts = {}
        self.__data_nodes = data_nodes

    # TODO Remove?
    # def add_data_node(self, data_node) -> None:
    #     new_node_id = len(self.__data_nodes)
    #     self.__data_nodes[new_node_id] = data_node

    def get_data_nodes(self) -> list[DataNode]:
        return self.__data_nodes

    def does_artifact_exist(self, artifact_name: str) -> bool:
        return artifact_name in self.__existing_artifacts.keys()

    def save_artifact(self, artifact: Artifact) -> None:
        assert (
            artifact.name not in self.__existing_artifacts.keys()
        ), f"Artifact named '{artifact.name}' already exists"

        data_nodes = self.__get_nodes_at_random()
        self.__existing_artifacts[artifact.name] = data_nodes

        for data_node in data_nodes:
            data_node.save_or_update_artifact.remote(artifact)

    def delete_artifact(self, artifact_name: Artifact) -> None:
        assert (
            artifact_name in self.__existing_artifacts.keys()
        ), f"Artifact named '{artifact_name}' doesn't exist"

        data_nodes = self.get_artifact_data_nodes(artifact_name)
        self.__existing_artifacts.pop(artifact_name)

        for data_node in data_nodes:
            data_node.delete_artifact.remote(artifact_name)

    def get_artifact_data_nodes(self, artifact_name: str) -> list[DataNode]:
        assert (
            artifact_name in self.__existing_artifacts.keys()
        ), f"Artifact named '{artifact_name}' doesn't exist"

        data_nodes = self.__existing_artifacts[artifact_name]
        return data_nodes

    def get_status(self) -> tuple[int, list[str]]:
        """Returns: `(data_nodes_number: int, saved_artifact_names: list[str])`"""
        data_nodes_number = len(self.__data_nodes)
        saved_artifact_names = list(self.__existing_artifacts.keys())

        return data_nodes_number, saved_artifact_names

    def __get_nodes_at_random(self) -> list[DataNode]:
        replica_count = min(self.__replica_count, len(self.__data_nodes))

        data_nodes = random.sample(self.__data_nodes, k=replica_count)
        random.shuffle(data_nodes)
        return data_nodes
