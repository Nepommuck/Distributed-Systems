import ray
import sys

from nodes.ClientNode import ClientNode
from nodes.DataNode import DataNode
from nodes.NameNode import NameNode
from repl.Repl import Repl


DEFAULT_DATA_NODES_NUMBER = 12
DEFAULT_REPLICA_COUNT = 3
DEFAULT_SEGMENTS_NUMBER = 3

USAGE = "launcher.py [REPLICA_COUNT] [SEGMENTS_NUMBER] [DATA_NODES_NUMBER]"


def main():
    replica_count, segments_number, data_nodes_number, error = parse_arguments(
        arguments=sys.argv[1:]
    )
    if error is None:
        error = validate_arguments(replica_count, segments_number, data_nodes_number)

    if error is not None:
        sys.stderr.write(f"Invalid argument error: {error}\nUsage: {USAGE}\n")
        sys.exit()

    replica_count, segments_number, data_nodes_number = inject_default_values(
        replica_count, segments_number, data_nodes_number
    )

    print(
        f"Starting Distributed-Artifact system with {data_nodes_number} data nodes..."
    )
    ray.init()
    data_nodes = [DataNode.remote(id) for id in range(data_nodes_number)]
    name_node = NameNode.remote(
        data_nodes, replica_count=replica_count, artifact_segment_number=segments_number
    )
    client = ClientNode.remote(name_node, data_nodes)

    repl = Repl(client)
    print(
        f"\nDistributed-Artifact system started with {data_nodes_number} data nodes. \n"
        + f"Each artifact will be splitted into {segments_number} parts and will have {replica_count} replicas."
    )
    repl.start()


def parse_number(input: str) -> tuple[int, str]:
    """Returns: (result: int, error: str)"""
    try:
        number = int(input)
    except ValueError:
        return None, f"'{input}' is not an integer"

    if number <= 0:
        return None, f"'{number}' is not a positive number"

    return number, None


def parse_arguments(arguments: list[str]) -> tuple[int, int, int, str]:
    """Returns: (replica_count: int, segments_number: int, data_nodes_number: int, error: str)"""

    if len(arguments) > 3:
        error = f"Server launcher takes up to 3 optional argument, but {len(arguments)} were given: '{arguments}'"

        return None, None, None, error

    if len(arguments) == 0:
        return None, None, None, None

    input_numbers = []
    for arg in arguments:
        number, error = parse_number(arg)
        if error is not None:
            return None, None, None, error

        input_numbers.append(number)

    replica_count, segments_number, data_nodes_number = input_numbers + [
        None for _ in range(3 - len(arguments))
    ]

    return replica_count, segments_number, data_nodes_number, None


def validate_arguments(
    replica_count: int, segments_number: int, data_nodes_number: int
) -> str:
    """Returns: (error: str)"""

    if (
        replica_count is not None
        and segments_number is not None
        and data_nodes_number is not None
        and replica_count * segments_number > data_nodes_number
    ):
        return f"`DATA_NODES_NUMBER` cannot be smaller, than expression `REPLICA_COUNT * SEGMENTS_NUMBER`"

    return None


def inject_default_values(
    replica_count: int, segments_number: int, data_nodes_number: int
) -> tuple[int, int, int, str]:
    if replica_count is None:
        replica_count = DEFAULT_REPLICA_COUNT

    if segments_number is None:
        segments_number = DEFAULT_SEGMENTS_NUMBER

    if data_nodes_number is None:
        data_nodes_number = max(
            DEFAULT_DATA_NODES_NUMBER, replica_count * segments_number
        )

    return replica_count, segments_number, data_nodes_number


if __name__ == "__main__":
    main()
