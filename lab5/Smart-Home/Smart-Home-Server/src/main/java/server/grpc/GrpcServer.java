package server.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import server.grpc.impl.DeviceServiceImpl;
import server.grpc.impl.FridgeServiceImpl;
import server.grpc.impl.LampServiceImpl;
import server.mock.DeviceMockService;
import server.mock.FridgeMockDao;
import server.mock.LampMockDao;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


public class GrpcServer {
    private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());
    private final DeviceMockService deviceService;
    private final LampMockDao lampDao;
    private final FridgeMockDao fridgeDao;

    private final String address;
    private final int port;

    private Server server;
    private SocketAddress socket;

    public GrpcServer(String address, int port, DeviceMockService deviceService, LampMockDao lampDao, FridgeMockDao fridgeDao) {
        this.lampDao = lampDao;
        this.deviceService = deviceService;
        this.address = address;
        this.port = port;
        this.fridgeDao = fridgeDao;
    }


    public void start() throws IOException {
        new InetSocketAddress(InetAddress.getByName(address), port);

        server = ServerBuilder.forPort(port)
                .executor(Executors.newFixedThreadPool(16))
                .addService(new DeviceServiceImpl(deviceService))
                .addService(new LampServiceImpl(lampDao))
                .addService(new FridgeServiceImpl(fridgeDao))
                .build()
                .start();

        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("Shutting down gRPC server...");
                GrpcServer.this.stop();
                System.err.println("Server shut down.");
            }
        });
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}
