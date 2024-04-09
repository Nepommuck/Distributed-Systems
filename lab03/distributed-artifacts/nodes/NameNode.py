import ray
import random

from model.Document import Document
from nodes.DataNode import DataNode


@ray.remote
class NameNode:
    def __init__(self, data_nodes: list[DataNode], replica_count: int = 4) -> None:
        self.__replica_count = replica_count
        # existing_documents: `document_name: str -> data_nodes: list[DataNode]`
        self.__existing_documents = {}
        self.__data_nodes = data_nodes

    # TODO Remove?
    # def add_data_node(self, data_node) -> None:
    #     new_node_id = len(self.__data_nodes)
    #     self.__data_nodes[new_node_id] = data_node

    def get_data_nodes(self) -> list[DataNode]:
        return self.__data_nodes

    def does_document_exist(self, document_name: str) -> bool:
        return document_name in self.__existing_documents.keys()

    def save_document(self, document: Document) -> None:
        assert (
            document.name not in self.__existing_documents.keys()
        ), f"Document named '{document.name}' already exists"

        data_nodes = self.__get_nodes_at_random()
        self.__existing_documents[document.name] = data_nodes

        for data_node in data_nodes:
            data_node.save_or_update_document.remote(document)

    def delete_document(self, document_name: Document) -> None:
        assert (
            document_name in self.__existing_documents.keys()
        ), f"Document named '{document_name}' doesn't exist"

        data_nodes = self.get_document_data_nodes(document_name)
        self.__existing_documents.pop(document_name)

        for data_node in data_nodes:
            data_node.delete_document.remote(document_name)

    def get_document_data_nodes(self, document_name: str) -> list[DataNode]:
        assert (
            document_name in self.__existing_documents.keys()
        ), f"Document named '{document_name}' doesn't exist"

        data_nodes = self.__existing_documents[document_name]
        return data_nodes

    def get_status(self) -> tuple[int, list[str]]:
        """Returns: `(data_nodes_number: int, saved_document_names: list[str])`"""
        data_nodes_number = len(self.__data_nodes)
        saved_document_names = list(self.__existing_documents.keys())

        return data_nodes_number, saved_document_names

    def __get_nodes_at_random(self) -> list[DataNode]:
        replica_count = min(self.__replica_count, len(self.__data_nodes))

        data_nodes = random.sample(self.__data_nodes, k=replica_count)
        random.shuffle(data_nodes)
        return data_nodes
