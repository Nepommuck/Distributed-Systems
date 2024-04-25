package server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


public class GrpcServer {
    private Server server;
    private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());

    public void start() throws IOException {
        final var port = 10_001;
        new InetSocketAddress(InetAddress.getByName("127.0.0.2"), port);

        server = ServerBuilder.forPort(port)
                .executor(Executors.newFixedThreadPool(16))
                .addService(new DemoServiceImpl())
                .build()
                .start();

        logger.info("GrpcServer started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("Shutting down gRPC server...");
                GrpcServer.this.stop();
                System.err.println("GrpcServer shut down.");
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
