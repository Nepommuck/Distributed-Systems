import grpc

from proto.Demo_pb2_grpc import DemoServiceStub
from proto.Demo_pb2 import IntArgs, StringArgs, EnumArgs, MyEnum

def main():
    channel = grpc.insecure_channel('127.0.0.2:10001')
    service_stub = DemoServiceStub(channel)

    while True:
        case = input("> ")

        if case == "1":
            service_stub.opInt(IntArgs(intArg1=996, intArg2=1410))

        if case == "2":
            service_stub.opInt(IntArgs(intArg1=420))

        if case == "3":
            service_stub.opString(StringArgs(stringArg1="Hello, ", stringArg2="World!"))

        if case == "4":
            service_stub.opString(StringArgs(stringArg1="Hello!"))

        if case == "5":
            service_stub.opEnum(EnumArgs(enumArg1=MyEnum.SECOND_OPTION, enumArg2=MyEnum.THIRD_OPTION))

        if case == "6":
            service_stub.opEnum(EnumArgs(enumArg1=MyEnum.SECOND_OPTION))


if __name__ == "__main__":
    main()
