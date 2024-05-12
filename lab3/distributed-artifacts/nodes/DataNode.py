import ray

from model.Artifact import ArtifactSegment
from nodes.util.MockDB import MockDB
from copy import deepcopy


@ray.remote
class DataNode:
    def __init__(self, node_id: int) -> None:
        self.__id = node_id
        self.__db = MockDB()

    def get_id(self) -> int:
        return self.__id

    def get_artifact_segment(self, artifact_name: str) -> ArtifactSegment:
        return self.__db.get(artifact_name)

    def save_or_update_segment(self, artifact_segment: ArtifactSegment) -> None:
        return self.__db.save(deepcopy(artifact_segment))

    def delete_artifact_segment(self, artifact_name: str) -> None:
        return self.__db.delete(artifact_name)

    def get_status(self) -> tuple[int, list[tuple[str, int]]]:
        """Returns: `(node_id: int, saved_segments_names_and_indexes: list[tuple[str, int]])`"""
        node_id = self.__id
        saved_artifact_names = self.__db.get_all_names_and_indexes()

        return node_id, saved_artifact_names
