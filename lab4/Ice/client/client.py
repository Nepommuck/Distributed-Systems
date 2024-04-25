import Ice
import Demo

class TestClient:
    def __init__(self):
        self.communicator = Ice.initialize()
        proxy = self.communicator.stringToProxy("test/test1:tcp -h 127.0.0.2 -p 10000")
        self.test_service = Demo.TestServicePrx.checkedCast(proxy)

    def shutdown(self):
        self.communicator.shutdown()

if __name__ == "__main__":
    client = TestClient()
    try:
        while True:
            case = input("> ")

            if case == "1":
                client.test_service.opInt(996, 1410)

            if case == "2":
                client.test_service.opInt(1939)

            if case == "3":
                client.test_service.opString("Required-String", "Optional-String")

            if case == "4":
                client.test_service.opString("Required-String")

            if case == "5":
                full_class = Demo.MyClass()
                full_class.a = 1
                full_class.b = 2
                client.test_service.opClass(full_class)

            if case == "6":
                empty_class = Demo.MyClass()
                empty_class.a = 1
                client.test_service.opClass(empty_class)

    except Ice.Exception as ex:
        print(ex)
    finally:
        client.shutdown()
