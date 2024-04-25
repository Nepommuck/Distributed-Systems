package server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        final GrpcServer server = new GrpcServer();

        server.start();
        server.blockUntilShutdown();
    }
}