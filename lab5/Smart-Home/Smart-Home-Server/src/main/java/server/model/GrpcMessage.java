package server.model;

public interface GrpcMessage<GrpcType> {
    GrpcType asGrpc();
}
