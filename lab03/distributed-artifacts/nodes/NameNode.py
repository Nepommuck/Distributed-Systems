import ray
import random

from model.Artifact import Artifact
from nodes.DataNode import DataNode


@ray.remote
class NameNode:
    def __init__(
        self,
        data_nodes: list[DataNode],
        replica_count: int,
        artifact_segment_number: int,
    ) -> None:
        assert replica_count * artifact_segment_number <= len(data_nodes), (
            f"Tried to initialize NameNode with too few data nodes ({len(data_nodes)}) "
            + f"compared to `replica_count * artifact_split_parts` ({replica_count * artifact_segment_number})"
        )

        self.__data_nodes = data_nodes
        self.__replica_count = replica_count
        self.__artifact_segment_number = artifact_segment_number
        # existing_artifacts: `artifact_name: str -> artifact_parts: list[data_nodes: list[DataNode]]`
        self.__existing_artifacts = {}

    def get_data_nodes(self) -> list[DataNode]:
        return self.__data_nodes

    def does_artifact_exist(self, artifact_name: str) -> bool:
        return artifact_name in self.__existing_artifacts.keys()

    def save_artifact(self, artifact: Artifact) -> None:
        assert (
            artifact.name not in self.__existing_artifacts.keys()
        ), f"Artifact named '{artifact.name}' already exists"

        data_nodes_per_segment = self.__get_nodes_at_random()
        self.__existing_artifacts[artifact.name] = data_nodes_per_segment

        segments = artifact.split_into_parts(self.__artifact_segment_number)

        for segment, data_nodes in zip(segments, data_nodes_per_segment):
            for node in data_nodes:
                node.save_or_update_segment.remote(segment)

    def delete_artifact(self, artifact_name: Artifact) -> None:
        assert (
            artifact_name in self.__existing_artifacts.keys()
        ), f"Artifact named '{artifact_name}' doesn't exist"

        data_nodes_per_segment = self.get_artifact_data_nodes(artifact_name)
        self.__existing_artifacts.pop(artifact_name)

        for data_nodes in data_nodes_per_segment:
            for node in data_nodes:
                node.delete_artifact_segment.remote(artifact_name)

    def get_artifact_data_nodes(self, artifact_name: str) -> list[list[DataNode]]:
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

    def __get_nodes_at_random(self) -> list[list[DataNode]]:
        """Returns: A `list` with `artifact_split_parts` elements. Each is a list of `replica_count` randomly selected `DataNode`s"""

        selected_data_nodes = random.sample(
            self.__data_nodes, k=self.__replica_count * self.__artifact_segment_number
        )
        random.shuffle(selected_data_nodes)

        result = [
            selected_data_nodes[i : i + self.__replica_count]
            for i in range(0, len(selected_data_nodes), self.__replica_count)
        ]

        return result
