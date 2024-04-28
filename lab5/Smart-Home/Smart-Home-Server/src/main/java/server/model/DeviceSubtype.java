package server.model;


public interface DeviceSubtype<GrpcType> extends GrpcMessage<GrpcType> {
    Device asDevice();
}
