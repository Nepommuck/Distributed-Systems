import ray
import sys

from Client import Client
from DataNode import DataNode
from Repl import Repl
from NameNode import NameNode


DEFAULT_DATA_NODES_NUMBER = 10


def main():
    node_number, error = parse_arguments(arguments=sys.argv[1:])

    if error is not None:
        sys.stderr.write(f"{error}\n")
        sys.exit()

    print(f"Starting Distributed-Artifact system with {node_number} data nodes...")
    ray.init()
    data_nodes = [DataNode.remote(id) for id in range(node_number)]
    name_node = NameNode.remote(data_nodes)
    client = Client.remote(name_node, data_nodes)

    repl = Repl(client)
    print(f"\nDistributed-Artifact system started with {node_number} data nodes")
    repl.start()


def parse_arguments(arguments) -> tuple[int, str]:
    if len(arguments) > 1:
        return (
            None,
            f"Server launcher takes only 1 optional argument: number of data nodes, but {len(arguments)} were given: '{arguments}'",
        )

    if len(arguments) == 0:
        return DEFAULT_DATA_NODES_NUMBER, None

    node_num_arg = arguments[0]
    try:
        node_num = int(node_num_arg)
    except ValueError:
        return (
            None,
            f"Expected integer argument (number of data nodes), but '{node_num_arg}' was provided",
        )

    if node_num <= 0:
        return (
            None,
            f"Expected positive integer argument (number of data nodes), but '{node_num}' was provided",
        )

    return node_num, None


if __name__ == "__main__":
    main()
