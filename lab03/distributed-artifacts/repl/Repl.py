import sys
import ray

from colorama import Fore, Style
from time import time

from nodes.ClientNode import ClientNode
from model.Artifact import Artifact


class Command:
    def __init__(
        self, name: str, argument_count: int, usage: str, description: str
    ) -> None:
        self.name = name
        self.argument_count = argument_count
        self.usage = usage
        self.description = description

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Command):
            return False
        return (
            self.name == other.name
            and self.argument_count == other.argument_count
            and self.usage == other.usage
            and self.description == other.description
        )


class AvailableCommands:
    help = Command(
        name="help",
        argument_count=0,
        usage="help",
        description="Display help",
    )
    upload = Command(
        name="upload",
        argument_count=2,
        usage="upload [artifact name] [content]",
        description="Upload new artifact",
    )
    read = Command(
        name="read",
        argument_count=1,
        usage="read [artifact name]",
        description="Read the content of an existing artifact",
    )
    modify = Command(
        name="modify",
        argument_count=2,
        usage="modify [artifact name] [new content]",
        description="Modify the content of an existing artifact",
    )
    delete = Command(
        name="delete",
        argument_count=1,
        usage="delete [artifact name]",
        description="Delete an existing artifact",
    )
    status = Command(
        name="status",
        argument_count=0,
        usage="status",
        description="Inspect cluster status",
    )
    exit = Command(
        name="exit",
        argument_count=0,
        usage="exit",
        description="Shutdown the server",
    )

    all = [help, upload, read, modify, delete, status, exit]


class Repl:
    def __init__(self, client: ClientNode) -> None:
        self.__client = client

    def start(self) -> None:
        print(
            f"\nWelcome to Distributed-Artifact REPL\nType `{AvailableCommands.help.usage}` for help"
        )
        while True:
            user_input = input(Fore.GREEN + "$ " + Style.RESET_ALL)
            command, validated_arguments, error = self.__parse_user_input(user_input)
            if error is not None:
                self.__print_error(error)
            else:
                self.__handle_user_input(command, validated_arguments)

    def __handle_user_input(
        self, command: Command, validated_arguments: list[str]
    ) -> str:
        if command == AvailableCommands.help:
            print(self.__get_help_message(), end="\n\n")

        elif command == AvailableCommands.upload:
            artifact_name, artifact_content = validated_arguments
            new_artifact = Artifact(artifact_name, artifact_content)
            error = ray.get(self.__client.save.remote(new_artifact))

            if error is not None:
                self.__print_error(
                    f"Failed to upload artifact '{artifact_name}': {error}"
                )
            else:
                print(f"Successfully uploaded artifact '{artifact_name}'")

        elif command == AvailableCommands.read:
            [artifact_name] = validated_arguments
            start_time = time()
            artifact, node_id, error = ray.get(self.__client.read.remote(artifact_name))
            execution_time = time() - start_time

            if error is not None:
                self.__print_error(
                    f"Failed to read artifact '{artifact_name}': {error}"
                )
            else:
                print(
                    f"Artifact '{artifact_name}' read successfully from DataNode#{node_id} in {execution_time:.2f}s\nContent:\n"
                    + Fore.BLUE
                    + artifact.content
                    + Style.RESET_ALL
                )

        elif command == AvailableCommands.modify:
            [artifact_name, new_content] = validated_arguments

            modified_artifact = Artifact(artifact_name, new_content)
            error = ray.get(self.__client.modify.remote(modified_artifact))

            if error is not None:
                self.__print_error(
                    f"Failed to modify artifact '{artifact_name}': {error}"
                )
            else:
                print(f"Artifact '{artifact_name}' modified successfully")

        elif command == AvailableCommands.delete:
            [artifact_name] = validated_arguments

            error = ray.get(self.__client.delete.remote(artifact_name))

            if error is not None:
                self.__print_error(
                    f"Failed to delete artifact '{artifact_name}': {error}"
                )
            else:
                print(f"Artifact '{artifact_name}' deleted successfully")

        elif command == AvailableCommands.status:
            name_node_status, data_nodes_statuses = ray.get(
                self.__client.get_status.remote()
            )

            print(
                self.__parse_nodes_status(name_node_status, data_nodes_statuses),
                end="\n\n",
            )

        elif command == AvailableCommands.exit:
            print("Shutting down Distributed-Artifact system...")
            ray.shutdown()
            sys.exit()

        elif command is not None:
            self.__print_error(
                f"Internal error. Unable to handle valid command `{command.name}`"
            )

    def __print_error(cls, error_message: str):
        sys.stderr.write(Fore.RED + f"ERROR: {error_message}\n" + Style.RESET_ALL)

    def __parse_user_input(cls, raw_user_input: str) -> tuple[Command, list[str], str]:
        """Returns: `(command: Command, validated_arguments: list[str], error: str)`"""

        def unexpected_arguments_msg(command: Command, args: list[str]) -> str:
            return f"Command '{command.name}' received unexpected arguments: {args}\nUsage: {command.usage}"

        def invalid_arguments_msg(
            command: Command, arg_count: int | None = None
        ) -> str:
            msg = f"Command '{command.name}' received invalid arguments"
            if arg_count is not None:
                msg += f" ({arg_count} instead of expected {command.argument_count})"
            msg += f"\nUsage: {command.usage}"
            return msg

        user_input = raw_user_input.split()

        # Empty input
        if len(user_input) == 0:
            return None, [], None

        command_name = user_input[0]
        arguments = user_input[1:]

        matching_commands = [
            command for command in AvailableCommands.all if command.name == command_name
        ]
        assert (
            len(matching_commands) <= 1
        ), f"Internal error: More than 1 command matched name {command_name}"

        if len(matching_commands) == 0:
            error = f"Unknown command `{command_name}`. Write `{AvailableCommands.help.name}` for help"
            return None, [], error

        command = matching_commands[0]
        if len(arguments) != command.argument_count:
            error = (
                unexpected_arguments_msg(command, arguments)
                if command.argument_count == 0
                else invalid_arguments_msg(command, arg_count=len(arguments))
            )
            return command, [], error

        return command, arguments, None

    def __get_help_message(cls) -> str:
        commands = AvailableCommands.all
        usage_length = max(len(command.usage) for command in commands)
        command_lines = [
            f"{command.usage:{usage_length}s}  -  {command.description}"
            for command in commands
        ]
        return "Available commands:\n" + "\n".join(command_lines)

    def __parse_nodes_status(
        cls,
        name_node_status: tuple[int, list[str]],
        data_nodes_statuses: list[tuple[int, list[str]]],
    ) -> str:
        def parse_name_node_status(status: tuple[int, list[str]]):
            data_nodes_number, saved_artifact_names = status

            connection_msg = f"Connected to {data_nodes_number} data nodes"
            storage_msg = f"Handles {len(saved_artifact_names)} artifacts:\n{saved_artifact_names}"

            return f"{connection_msg}\n{storage_msg}"

        def parse_data_node_status(status: tuple[int, list[str]]):
            node_id, saved_artifact_names = status

            return (
                Fore.BLUE
                + f"DataNode#{node_id}"
                + Style.RESET_ALL
                + f" stores {len(saved_artifact_names)} artifacts:\n{sorted(saved_artifact_names)}"
            )

        name_node_msg = (
            Fore.BLUE
            + "NameNode"
            + Style.RESET_ALL
            + f":\n{parse_name_node_status(name_node_status)}"
        )

        data_nodes_statuses_msgs = [
            parse_data_node_status(status) for status in sorted(data_nodes_statuses)
        ]
        data_nodes_msg = "\n".join(data_nodes_statuses_msgs)

        return f"{name_node_msg}\n\n{data_nodes_msg}"
