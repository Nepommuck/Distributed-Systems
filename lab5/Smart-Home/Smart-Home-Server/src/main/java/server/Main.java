package server;

import server.grpc.GrpcServer;
import server.mock.DeviceMockService;
import server.mock.FridgeMockDao;
import server.mock.LampMockDao;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class Main {
    private static Optional<Integer> parsePort(String[] args) {
        if (args.length == 0) {
            return Optional.empty();
        } else if (args.length != 1) {
            throw new IllegalArgumentException("Expected 1 optional argument: PORT_NUMBER, but received " + args.length);
        } else {
            try {
                final var port = Integer.parseInt(args[0]);
                if (port < 1024 || port > 65535) {
                    throw new IllegalArgumentException("Invalid port number, should be in range 1024-65535");
                }
                return Optional.of(port);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(args[0] + " is not an integer");
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            final var port = parsePort(args).orElse(Config.DEFAULT_PORT);
            final var lampDao = new LampMockDao();
            final var fridgeDao = new FridgeMockDao();
            final var deviceService = new DeviceMockService(lampDao, fridgeDao);

            final GrpcServer server = new GrpcServer(
                    Config.IP_ADDRESS,
                    port,
                    deviceService,
                    lampDao,
                    fridgeDao
            );
            server.start();
            server.blockUntilShutdown();
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage() + "\n");
        }
    }
}
