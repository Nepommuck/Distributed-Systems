package server.model;


import grpc.gen.Device.DeviceId;
import grpc.gen.Device.DeviceInfo;
import grpc.gen.Device.DeviceType;
import grpc.gen.Device.PowerState;

public record Device(int id, DeviceType type, PowerState powerState) implements GrpcMessage<DeviceInfo> {
    @Override
    public DeviceInfo asGrpc() {
        return DeviceInfo.newBuilder()
                .setId(DeviceId.newBuilder().setValue(id))
                .setType(type)
                .setPowerState(powerState)
                .build();
    }
}
