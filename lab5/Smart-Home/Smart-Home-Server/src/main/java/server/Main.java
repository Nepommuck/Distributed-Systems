package server;

import server.grpc.GrpcServer;
import server.mock.DeviceMockService;
import server.mock.FridgeMockDao;
import server.mock.LampMockDao;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        final var lampDao = new LampMockDao();
        final var fridgeDao = new FridgeMockDao();
        final var deviceService = new DeviceMockService(lampDao, fridgeDao);

        final GrpcServer server = new GrpcServer(
                Config.IP_ADDRESS,
                Config.PORT,
                deviceService,
                lampDao,
                fridgeDao
        );
        server.start();
        server.blockUntilShutdown();
    }
}
