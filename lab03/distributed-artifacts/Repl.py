import sys
import ray

from colorama import Fore, Style
from time import time

from Client import Client
from Document import Document


class Command:
    def __init__(self, name, usage, description) -> None:
        self.name = name
        self.usage = usage
        self.description = description

class AvailableCommands:
    help = Command(
        name="help",
        usage="help",
        description="Display help",
    )
    upload = Command(
        name="upload",
        usage="upload [document name] [content]",
        description="Upload new document",
    )
    read = Command(
        name="read",
        usage="read [document name]",
        description="Read the content of an existing document",
    )
    status = Command(
        name="status",
        usage="status",
        description="Inspect cluster status",
    )
    exit = Command(
        name="exit",
        usage="exit",
        description="Shutdown the server",
    )

    all = [help, upload, read, status, exit]


class Repl:
    def __init__(self, client: Client) -> None:
        self.__client = client

    def start(self) -> None:
        print(f"\nWelcome to Distributed-Artifact REPL\nType `{AvailableCommands.help.usage}` for help")
        while True:
            user_input = input(Fore.GREEN + "$ " + Style.RESET_ALL)
            command, validated_arguments, error = self.__parse_user_input(user_input)
            if error is not None:
                self.__print_error(error)
            else:
                self.__handle_user_input(command, validated_arguments)


    def __handle_user_input(self, command, validated_arguments) -> str:
        if command == AvailableCommands.help.name:
            print(self.__get_help_message(), end="\n\n")
        
        elif command == AvailableCommands.upload.name:
            document_name, document_content = validated_arguments
            new_document = Document(document_name, document_content)
            error = ray.get(self.__client.save.remote(new_document))

            if error is not None:
                self.__print_error(f"Failed to upload document '{document_name}': {error}")
            else:
                print(f"Successfully uploaded document '{document_name}'")
    
        elif command == AvailableCommands.read.name:
            [document_name] = validated_arguments
            start_time = time()
            document, node_id, error = ray.get(self.__client.read.remote(document_name))
            execution_time = time() - start_time

            if error is not None:
                self.__print_error(f"Failed to read document '{document_name}': {error}")
            else:
                print(f"Document '{document_name}' successfully read from DataNode#{node_id} in {execution_time:.2f}s\nContent:\n" + Fore.BLUE + document.content + Style.RESET_ALL)

        elif command == AvailableCommands.status.name:
            name_node_status, data_nodes_statuses = ray.get(self.__client.get_status.remote())
            
            print(self.__parse_nodes_status(name_node_status, data_nodes_statuses), end="\n\n")

        elif command == AvailableCommands.exit.name:
            print("Shutting down Distributed-Artifact system...")
            ray.shutdown()
            sys.exit()

        elif command is not None:
            self.__print_error(f"Internal error. Unable to handle valid command `{command}`")
    

    def __print_error(cls, error_message: str):
            sys.stderr.write(Fore.RED + f"ERROR: {error_message}\n" + Style.RESET_ALL)

    def __parse_user_input(cls, raw_user_input: str) -> tuple[str, list[str], str]:
        """Returns: `(command: str, validated_arguments: list[str], error: str)`"""
        
        def unexpected_arguments_msg(command: Command, args: list[str]) -> str:
            return f"Command '{command.name}' received unexpected arguments: {args}\nUsage: {command.usage}"
        
        def invalid_arguments_msg(command: Command, detailed_error: str) -> str:
            msg = f"Command '{command.name}' received invalid arguments"
            msg += f": {detailed_error}" if detailed_error is not None else "."
            msg += f"\nUsage: {command.usage}"
            return msg
        
        user_input = raw_user_input.split()
        
        # Empty input
        if len(user_input) == 0:
            return None, [], None
        
        command = user_input[0]
        arguments = user_input[1:]

        if command == AvailableCommands.help.name:
            if len(arguments) != 0:
                error = unexpected_arguments_msg(AvailableCommands.help, arguments)
                return command, [], error
            
            return command, [], None
        
        if command == AvailableCommands.upload.name:
            if len(arguments) != 2:
                error = invalid_arguments_msg(AvailableCommands.exit)
                return command, [], error
            
            document_name, content = arguments
            return command, [document_name, content], None
        
        if command == AvailableCommands.read.name:
            if len(arguments) != 1:
                error = invalid_arguments_msg(AvailableCommands.read)
                return command, [], error
            
            [document_name] = arguments
            return command, [document_name], None
        
        if command == AvailableCommands.status.name:
            if len(arguments) != 0:
                error = unexpected_arguments_msg(AvailableCommands.status, arguments)
                return command, [], error
            
            return command, [], None

        if command == AvailableCommands.exit.name:
            if len(arguments) != 0:
                error = unexpected_arguments_msg(AvailableCommands.exit, arguments)
                return command, [], error
            
            return command, [], None
        
        error = f"Unknown command `{command}`. Write `{AvailableCommands.help.name}` for help"
        return None, [], error
    

    def __get_help_message(cls) -> str:
        commands = AvailableCommands.all
        usage_length = max(len(command.usage) for command in commands)
        command_lines = [f"{command.usage:{usage_length}s}  -  {command.description}" for command in commands]
        return "Available commands:\n" + "\n".join(command_lines)
    
    def __parse_nodes_status(
        cls, 
        name_node_status: tuple[int, list[str]], 
        data_nodes_statuses: list[tuple[int, list[str]]],
    ) -> str:
        def parse_name_node_status(status: tuple[int, list[str]]):
            data_nodes_number, saved_document_names = status

            connection_msg = f"Connected to {data_nodes_number} data nodes"
            storage_msg = f"Handles {len(saved_document_names)} documents:\n{saved_document_names}"

            return f"{connection_msg}\n{storage_msg}"

        def parse_data_node_status(status: tuple[int, list[str]]):
            node_id, saved_document_names = status

            return f"NameNode#{node_id} stores {len(saved_document_names)} documents:\n{sorted(saved_document_names)}"

        name_node_msg = f"NameNode:\n{parse_name_node_status(name_node_status)}"

        data_nodes_statuses_msgs = [parse_data_node_status(status) for status in sorted(data_nodes_statuses)]
        data_nodes_msg = "\n".join(data_nodes_statuses_msgs)

        return f"{name_node_msg}\n\n{data_nodes_msg}"
