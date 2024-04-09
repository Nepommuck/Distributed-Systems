import ray

from DataNode import DataNode
from Document import Document
from NameNode import NameNode

@ray.remote
class Client:
    def __init__(self, name_node: NameNode, data_nodes: list[DataNode]) -> None:
        self.__name_node = name_node
        self.__data_nodes = data_nodes
    
    def read(self, document_name: str) -> tuple[Document, int, str]:
        '''Returns: `(document: Document, node_id: int, error: str)`'''
        does_document_exist = ray.get(self.__name_node.does_document_exist.remote(document_name))
        if not does_document_exist:
            error = f"Document named '{document.name}' doesn't exist"
            return None, None, error
        
        data_nodes = ray.get(self.__name_node.get_document_data_nodes.remote(document_name))

        all_ray_ids = [node.get_document.remote(document_name) for node in data_nodes]
        [ready_ray_id], _ = ray.wait(all_ray_ids, num_returns=1)
        ready_index = all_ray_ids.index(ready_ray_id)

        ready_node_id = ray.get(data_nodes[ready_index].get_id.remote())
        document = ray.get(ready_ray_id)

        return document, ready_node_id, None
    
    def save(self, document: Document) -> str:
        '''Returns error message on failure'''
        already_exists = ray.get(self.__name_node.does_document_exist.remote(document.name))
        if already_exists:
            error = f"Document named '{document.name}' already exists"
            return error
        
        self.__name_node.save_document.remote(document)
    
    def modify(self, document: Document) -> str:
        '''Returns error message on failure'''
        does_document_exist = ray.get(self.__name_node.does_document_exist.remote(document.name))
        if not does_document_exist:
            error = f"Document named '{document.name}' doesn't exist"
            return error
        
        data_nodes = ray.get(self.__name_node.get_document_data_nodes.remote(document.name))

        for data_node in data_nodes:
            data_node.save_or_update_document.remote(document)
        return None        
    
    def get_status(self) -> tuple[tuple[int, list[str]], list[tuple[int, list[str]]]]:
        '''Returns: `(name_node_status, data_nodes_statuses)`.
        `name_node_status -> (data_nodes_number: int, saved_document_names: list[str])`
        `data_nodes_statuses -> list[(node_id: int, saved_document_names: list[str])]`'''
    
        name_node_status_ray_id = self.__name_node.get_status.remote()
        data_nodes_statuses_ray_ids = [data_node.get_status.remote() for data_node in self.__data_nodes]

        name_node_status = ray.get(name_node_status_ray_id)
        data_nodes_statuses = ray.get(data_nodes_statuses_ray_ids)

        return name_node_status, data_nodes_statuses
