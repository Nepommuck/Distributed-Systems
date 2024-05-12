from math import floor


class Artifact:
    def __init__(self, name: str, content: str) -> None:
        assert (
            name is not None and content is not None
        ), "Artifact constructor arguments cannot be `None`"
        self.name = name
        self.content = content

    def recreate_from_segments(segments: list["ArtifactSegment"]) -> "Artifact":
        content = "".join([segment.content for segment in segments])

        return Artifact(name=segments[0].artifact_name, content=content)

    def split_into_parts(self, segments_number: int) -> list["ArtifactSegment"]:
        assert segments_number > 0, "`segments_number` must be positive"

        segment_length = floor(len(self.content) / segments_number)
        remaining_length = len(self.content) % segments_number

        segment_final_lengths = [
            segment_length + (1 if i < remaining_length else 0)
            for i in range(segments_number)
        ]

        start_indexes = [0]
        for segment_length in segment_final_lengths[:-1]:
            start_indexes.append(start_indexes[-1] + segment_length)

        contents = [
            self.content[start : start + length]
            for start, length in zip(start_indexes, segment_final_lengths)
        ]
        result = [
            ArtifactSegment(self.name, segment_index=i, content=content)
            for (i, content) in enumerate(contents)
        ]

        return result


class ArtifactSegment:
    def __init__(self, artifact_name: str, segment_index: int, content: str) -> None:
        assert (
            artifact_name is not None
            and content is not None
            and segment_index is not None
        ), "ArtifactPart constructor arguments cannot be `None`"
        self.artifact_name = artifact_name
        self.index = segment_index
        self.content = content
