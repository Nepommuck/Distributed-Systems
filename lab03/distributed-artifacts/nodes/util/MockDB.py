import random
import time

from model.Artifact import Artifact


class MockDB:
    def __init__(self) -> None:
        # saved_artifacts: tuple[str, Artifact]
        self.__saved_artifacts = {}

    def __random_sleep(cls, avg_sleep_seconds: float) -> None:
        sleep_duration = random.uniform(avg_sleep_seconds / 2, 2 * avg_sleep_seconds)
        time.sleep(sleep_duration)

    def get(self, artifact_name: str) -> Artifact:
        self.__random_sleep(1)
        return self.__saved_artifacts.get(artifact_name)

    def get_all_names(self) -> list[str]:
        self.__random_sleep(0.1)
        return self.__saved_artifacts.keys()

    def save(self, artifact: Artifact) -> None:
        self.__random_sleep(3)
        self.__saved_artifacts[artifact.name] = artifact

    def delete(self, artifact_name: str) -> None:
        self.__random_sleep(1)
        self.__saved_artifacts.pop(artifact_name)
