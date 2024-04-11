import random
import time

from model.Artifact import ArtifactSegment


class MockDB:
    def __init__(self) -> None:
        # saved_artifacts: artifact_name: str -> segment: ArtifactSegment
        self.__saved_segments = {}

    def __random_sleep(cls, avg_sleep_seconds: float) -> None:
        sleep_duration = random.uniform(avg_sleep_seconds / 2, 2 * avg_sleep_seconds)
        time.sleep(sleep_duration)

    def get(self, artifact_name: str) -> ArtifactSegment:
        self.__random_sleep(0.5)
        return self.__saved_segments.get(artifact_name)

    def get_all_names_and_indexes(self) -> list[tuple[str, int]]:
        self.__random_sleep(0.05)
        all_segments = self.__saved_segments.values()
        names_and_indexes = [(segment.artifact_name, segment.index) for segment in all_segments]

        return names_and_indexes

    def save(self, new_segment: ArtifactSegment) -> None:
        self.__random_sleep(0.2)
        self.__saved_segments[new_segment.artifact_name] = new_segment

    def delete(self, artifact_name: str) -> None:
        self.__random_sleep(0.2)
        self.__saved_segments.pop(artifact_name)
