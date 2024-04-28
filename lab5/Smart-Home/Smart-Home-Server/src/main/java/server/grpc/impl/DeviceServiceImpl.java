package server.grpc.impl;

import grpc.gen.Device.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import server.mock.DeviceMockService;
import server.model.Device;
import server.model.exception.NotFoundException;
import server.model.exception.StateAlreadySetException;

import java.util.Comparator;

public class DeviceServiceImpl extends DeviceServiceGrpc.DeviceServiceImplBase {
    private final DeviceMockService deviceService;

    public DeviceServiceImpl(DeviceMockService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public void getDevices(Empty request, StreamObserver<GetDevicesResponse> responseObserver) {
        var response = GetDevicesResponse.newBuilder().addAllDevices(
                deviceService.getAll()
                        .sorted(Comparator.comparingInt(Device::id))
                        .map(Device::asGrpc).toList()
        ).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getDevice(DeviceId deviceId, StreamObserver<DeviceInfo> responseObserver) {
        deviceService.getById(deviceId.getValue()).ifPresentOrElse(
                device -> {
                    responseObserver.onNext(device.asGrpc());
                    responseObserver.onCompleted();
                }, () -> {
                    responseObserver.onError(Status.NOT_FOUND
                            .withDescription("Device with id " + deviceId.getValue() + " not found")
                            .asException());
                }
        );
    }

    @Override
    public void turnOn(DeviceId deviceId, StreamObserver<Empty> responseObserver) {
        changeState(deviceId.getValue(), PowerState.ON, responseObserver);
    }

    @Override
    public void turnOff(DeviceId deviceId, StreamObserver<Empty> responseObserver) {
        changeState(deviceId.getValue(), PowerState.OFF, responseObserver);
    }

    private void changeState(int deviceId, PowerState newState, StreamObserver<Empty> responseObserver) {
        try {
            deviceService.changePowerState(deviceId, newState);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asException());
        } catch (StateAlreadySetException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asException());
        } catch (IllegalStateException | InternalError e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asException());
        }
    }
}
