import random
import time

from model.Document import Document


class MockDB:
    def __init__(self) -> None:
        # saved_documents: tuple[str, Document]
        self.__saved_documents = {}

    def __random_sleep(cls, avg_sleep_seconds: float) -> None:
        sleep_duration = random.uniform(avg_sleep_seconds / 2, 2 * avg_sleep_seconds)
        time.sleep(sleep_duration)

    def get(self, document_name: str) -> Document:
        self.__random_sleep(1)
        return self.__saved_documents.get(document_name)

    def get_all_names(self) -> list[str]:
        self.__random_sleep(0.1)
        return self.__saved_documents.keys()

    def save(self, document: Document) -> None:
        self.__random_sleep(3)
        self.__saved_documents[document.name] = document

    def delete(self, document_name: str) -> None:
        self.__random_sleep(1)
        self.__saved_documents.pop(document_name)
