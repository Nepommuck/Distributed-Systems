import sys

from colorama import Fore, Style
from time import time

from repl.Command import AvailableCommands, Command
from repl.ResponseParser import ResponseParser

from grpc import RpcError

from proto.Device_pb2 import Empty, DeviceId
from proto.Device_pb2_grpc import DeviceServiceStub
from proto.Lamp_pb2_grpc import LampServiceStub
from proto.Lamp_pb2 import Brightness, SetBrightnessRequest
from proto.Fridge_pb2_grpc import FridgeServiceStub
from proto.Fridge_pb2 import Temperature, SetTemperatureRequest


class Repl:
    def __init__(
        self,
        device_service_stub: DeviceServiceStub,
        lamp_service_stub: LampServiceStub,
        fridge_service_stub: FridgeServiceStub,
    ) -> None:
        self.__device_service_stub = device_service_stub
        self.__lamp_service_stub = lamp_service_stub
        self.__fridge_service_stub = fridge_service_stub

    def start(self) -> None:
        print(
            f"\nWelcome to Smart-Home Client REPL\nType `{AvailableCommands.help.usage}` for help"
        )
        while True:
            user_input = input(Fore.GREEN + "$ " + Style.RESET_ALL)
            command, validated_arguments, error = self.__parse_user_input(user_input)
            if error is not None:
                self.__print_error(error)
            else:
                self.__handle_user_input(command, validated_arguments)

    def __handle_user_input(self, command: Command, validated_arguments: list) -> str:
        exec = None

        if command == AvailableCommands.help:
            print(self.__get_help_message(), end="\n\n")

        elif command == AvailableCommands.get_all_devices:
            exec = lambda: ResponseParser.parse_get_devices_response(
                self.__device_service_stub.getDevices(Empty())
            )

        elif command == AvailableCommands.get_device:
            [device_id] = validated_arguments
            exec = lambda: ResponseParser.parse_device_info(
                self.__device_service_stub.getDevice(DeviceId(value=device_id))
            )

        elif command == AvailableCommands.turn_on:
            [device_id] = validated_arguments
            exec = lambda: ResponseParser.parse_empty(
                self.__device_service_stub.turnOn(DeviceId(value=device_id))
            )

        elif command == AvailableCommands.turn_off:
            [device_id] = validated_arguments
            exec = lambda: ResponseParser.parse_empty(
                self.__device_service_stub.turnOff(DeviceId(value=device_id))
            )

        elif command == AvailableCommands.lamp_get:
            [device_id] = validated_arguments
            exec = lambda: ResponseParser.parse_lamp_info(
                self.__lamp_service_stub.getLampInfo(DeviceId(value=device_id))
            )

        elif command == AvailableCommands.lamp_set_brightness:
            [device_id, brightness_level, temperature_K] = validated_arguments

            exec = lambda: ResponseParser.parse_empty(
                self.__lamp_service_stub.setBrightness(
                    SetBrightnessRequest(
                        deviceId=DeviceId(value=device_id),
                        newBrightness=Brightness(
                            brightnessLevel=brightness_level, temperatureK=temperature_K
                        ),
                    )
                )
            )

        elif command == AvailableCommands.fridge_get:
            [device_id] = validated_arguments
            exec = lambda: ResponseParser.parse_fridge_info(
                self.__fridge_service_stub.getFridgeInfo(DeviceId(value=device_id))
            )

        elif command == AvailableCommands.fridge_set_temperature:
            [device_id, fridge_temperature, freezer_temperature] = validated_arguments

            exec = lambda: ResponseParser.parse_empty(
                self.__fridge_service_stub.setDesiredTemperature(
                    SetTemperatureRequest(
                        deviceId=DeviceId(value=device_id),
                        newTemperature=Temperature(
                            fridgeTemperatureC=fridge_temperature,
                            freezerTemperatureC=freezer_temperature,
                        ),
                    )
                )
            )

        elif command == AvailableCommands.exit:
            print("Shutting down Smart-Home client system...")
            sys.exit()

        elif command is not None:
            self.__print_error(
                f"Internal error. Unable to handle valid command `{command.name}`"
            )

        if exec is not None:
            try:
                response = exec()
                print("gRPC response:", response, sep="\n\n", end="\n\n")
            except RpcError as rpc_error:
                self.__print_error(
                    f"gRPC ERROR\nSTATUS: {rpc_error.code()}\nDETAILS: {rpc_error.details()}\n"
                )

    def __print_error(cls, error_message: str):
        sys.stderr.write(Fore.RED + f"ERROR: {error_message}" + Style.RESET_ALL + "\n")

    def __parse_user_input(cls, raw_user_input: str) -> tuple[Command, list, str]:
        """Returns: `(command: Command, validated_arguments: list, error: str)`"""

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

        parsed_arguments = []
        if command.argument_count > 0:
            parsed_arguments, error = command.parse_arguments_fun(arguments)

            if error is not None:
                return command, [], error

        return command, parsed_arguments, None

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
        data_nodes_statuses: list[tuple[int, list[tuple[str, int]]]],
    ) -> str:
        def parse_name_node_status(status: tuple[int, list[str]]):
            data_nodes_number, saved_artifact_names = status

            connection_msg = f"Connected to {data_nodes_number} data nodes"
            storage_msg = f"Handles {len(saved_artifact_names)} artifacts:\n{saved_artifact_names}"

            return f"{connection_msg}\n{storage_msg}"

        def parse_data_node_status(status: tuple[int, list[tuple[str, int]]]):
            node_id, saved_segments_names_and_indexes = status

            parsed_saved_segments = sorted(
                [
                    f"{name}#{index}"
                    for (name, index) in saved_segments_names_and_indexes
                ]
            )

            return (
                Fore.BLUE
                + f"DataNode#{node_id}"
                + Style.RESET_ALL
                + f" stores {len(saved_segments_names_and_indexes)} segments:\n{parsed_saved_segments}"
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
