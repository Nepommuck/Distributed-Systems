import grpc
import sys

from colorama import Fore, Style
from proto import Device_pb2_grpc, Lamp_pb2_grpc, Fridge_pb2_grpc

from config import Config
from repl.Repl import Repl


def parse_port(args: list[str]) -> tuple[int, str]:
    if len(args) == 0:
        return None, None
    elif len(args) != 1:
        return (
            None,
            f"Expected 1 optional argument: PORT_NUMBER, but received {len(args)}",
        )
    else:
        try:
            port = int(args[0])
            if port < 1024 or port > 65535:
                return None, "Invalid port number, should be in range 1024-65535"
            return port, None
        except ValueError:
            return None, f"{args[0]} is not an integer"


def main():
    port_opt, error = parse_port(sys.argv[1:])

    if error is not None:
        sys.stderr.write(Fore.RED + error + Style.RESET_ALL + "\n")
        sys.exit()

    port = port_opt if port_opt is not None else Config.host_default_port

    channel = grpc.insecure_channel(f"{Config.host_ip}:{port}")
    device_stub = Device_pb2_grpc.DeviceServiceStub(channel)
    lamp_stub = Lamp_pb2_grpc.LampServiceStub(channel)
    fridge_stub = Fridge_pb2_grpc.FridgeServiceStub(channel)

    repl = Repl(device_stub, lamp_stub, fridge_stub)

    repl.start()


if __name__ == "__main__":
    main()
