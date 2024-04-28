import grpc

from proto import Device_pb2_grpc, Lamp_pb2_grpc, Fridge_pb2_grpc

from repl.Repl import Repl


def main():
    channel = grpc.insecure_channel('127.0.0.5:50051')
    device_stub = Device_pb2_grpc.DeviceServiceStub(channel)
    lamp_stub = Lamp_pb2_grpc.LampServiceStub(channel)
    fridge_stub = Fridge_pb2_grpc.FridgeServiceStub(channel)

    repl = Repl(device_stub, lamp_stub, fridge_stub)

    repl.start()


if __name__ == "__main__":
    main()
