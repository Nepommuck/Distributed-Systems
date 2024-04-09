import ray

from Document import Document
from MockDB import MockDB


@ray.remote
class DataNode:
    def __init__(self, node_id: int) -> None:
        self.__id = node_id
        self.__db = MockDB()

    def get_id(self) -> int:
        return self.__id

    def get_document(self, document_name: str) -> Document:
        return self.__db.get(document_name)

    def save_or_update_document(self, document: Document) -> None:
        return self.__db.save(document)

    def delete_document(self, document_name: str) -> None:
        return self.__db.delete(document_name)

    def get_status(self) -> tuple[int, list[str]]:
        """Returns: `(node_id: int, saved_document_names: list[str])`"""
        node_id = self.__id
        saved_document_names = self.__db.get_all_names()

        return node_id, saved_document_names
