import ray

from model.Artifact import Artifact
from nodes.util.MockDB import MockDB


@ray.remote
class DataNode:
    def __init__(self, node_id: int) -> None:
        self.__id = node_id
        self.__db = MockDB()

    def get_id(self) -> int:
        return self.__id

    def get_artifact(self, artifact_name: str) -> Artifact:
        return self.__db.get(artifact_name)

    def save_or_update_artifact(self, artifact: Artifact) -> None:
        return self.__db.save(artifact)

    def delete_artifact(self, artifact_name: str) -> None:
        return self.__db.delete(artifact_name)

    def get_status(self) -> tuple[int, list[str]]:
        """Returns: `(node_id: int, saved_artifact_names: list[str])`"""
        node_id = self.__id
        saved_artifact_names = self.__db.get_all_names()

        return node_id, saved_artifact_names
