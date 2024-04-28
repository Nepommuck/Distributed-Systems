package server.grpc.impl;

import grpc.gen.Device.DeviceId;
import grpc.gen.Device.Empty;
import grpc.gen.Lamp.Brightness;
import grpc.gen.Lamp.LampInfo;
import grpc.gen.Lamp.LampServiceGrpc;
import grpc.gen.Lamp.SetBrightnessRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import server.Config;
import server.mock.LampMockDao;
import server.model.exception.NotFoundException;

import java.util.Optional;

public class LampServiceImpl extends LampServiceGrpc.LampServiceImplBase {
    private final LampMockDao dao;

    public LampServiceImpl(LampMockDao lampDao) {
        this.dao = lampDao;
    }

    @Override
    public void getLampInfo(DeviceId deviceId, StreamObserver<LampInfo> responseObserver) {
        try {
            final var lamp = dao.getById(deviceId.getValue())
                    .orElseThrow(() -> new NotFoundException("No lamp found with id: " + deviceId.getValue()));

            responseObserver.onNext(lamp.asGrpc());
            responseObserver.onCompleted();
        } catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asException());
        }
    }

    @Override
    public void setBrightness(SetBrightnessRequest request, StreamObserver<Empty> responseObserver) {
        try {
            var newBrightness = request.getNewBrightness();
            validateBrightness(newBrightness).ifPresentOrElse(
                    errorMsg -> responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription(errorMsg)
                            .asException()),
                    () -> {
                        dao.setBrightness(
                                request.getDeviceId().getValue(),
                                newBrightness.getBrightnessLevel(),
                                newBrightness.getTemperatureK()
                        );
                        responseObserver.onNext(Empty.getDefaultInstance());
                        responseObserver.onCompleted();
                    }
            );
        } catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asException());
        }
    }

    private Optional<String> validateBrightness(Brightness newBrightness) {
        var brightnessLevel = newBrightness.getBrightnessLevel();
        var temperatureK = newBrightness.getTemperatureK();

        if (brightnessLevel != 0f &&
                (brightnessLevel < Config.MIN_BRIGHTNESS_LEVEL || brightnessLevel > Config.MAX_BRIGHTNESS_LEVEL))
            return Optional.of("Invalid brightness level: Must be [" + Config.MIN_BRIGHTNESS_LEVEL + ", "
                    + Config.MIN_BRIGHTNESS_LEVEL + "], but was " + brightnessLevel);

        if (temperatureK != 0f && (temperatureK < Config.MIN_TEMPERATURE_K || temperatureK > Config.MAX_TEMPERATURE_K))
            return Optional.of("Invalid temperature[K]: Must be [" + Config.MIN_TEMPERATURE_K + ", "
                    + Config.MAX_TEMPERATURE_K + "], but was " + temperatureK);

        return Optional.empty();
    }
}
