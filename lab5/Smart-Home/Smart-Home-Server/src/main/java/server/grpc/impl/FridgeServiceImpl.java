package server.grpc.impl;

import grpc.gen.Device.DeviceId;
import grpc.gen.Device.Empty;
import grpc.gen.Fridge.FridgeInfo;
import grpc.gen.Fridge.FridgeServiceGrpc;
import grpc.gen.Fridge.SetTemperatureRequest;
import grpc.gen.Fridge.Temperature;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import server.Config;
import server.mock.FridgeMockDao;
import server.model.exception.NotFoundException;

import java.util.Optional;

public class FridgeServiceImpl extends FridgeServiceGrpc.FridgeServiceImplBase {
    private final FridgeMockDao dao;

    public FridgeServiceImpl(FridgeMockDao fridgeDao) {
        this.dao = fridgeDao;
    }

    @Override
    public void getFridgeInfo(DeviceId deviceId, StreamObserver<FridgeInfo> responseObserver) {
        try {
            final var fridge = dao.getById(deviceId.getValue())
                    .orElseThrow(() -> new NotFoundException("No fridge found with id: " + deviceId.getValue()));

            responseObserver.onNext(fridge.asGrpc());
            responseObserver.onCompleted();
        } catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asException());
        }
    }

    @Override
    public void setDesiredTemperature(SetTemperatureRequest request, StreamObserver<Empty> responseObserver) {
        System.out.println(request.getDeviceId().getValue());
        final var newDesiredTemperature = request.getNewTemperature();
        try {
            validateTemperature(newDesiredTemperature).ifPresentOrElse(
                    errorMsg -> responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription(errorMsg)
                            .asException()),
                    () -> {
                        dao.setDesiredTemperature(
                                request.getDeviceId().getValue(),
                                newDesiredTemperature.getFridgeTemperatureC(),
                                newDesiredTemperature.getFreezerTemperatureC()
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

    private Optional<String> validateTemperature(Temperature temperature) {
        var fridgeTemperatureC = temperature.getFridgeTemperatureC();
        var freezerTemperatureC = temperature.getFreezerTemperatureC();

        if (fridgeTemperatureC < Config.MIN_FRIDGE_TEMPERATURE_C || fridgeTemperatureC > Config.MAX_FRIDGE_TEMPERATURE_C)
            return Optional.of("Invalid fridge temperature: Must be [" + Config.MIN_FRIDGE_TEMPERATURE_C + "°C, "
                    + Config.MAX_FRIDGE_TEMPERATURE_C + "°C], but was " + fridgeTemperatureC);

        if (freezerTemperatureC < Config.MIN_FREEZER_TEMPERATURE_C || freezerTemperatureC > Config.MAX_FREEZER_TEMPERATURE_C)
            return Optional.of("Invalid fridge temperature: Must be [" + Config.MIN_FREEZER_TEMPERATURE_C + "°C, "
                    + Config.MAX_FREEZER_TEMPERATURE_C + "°C], but was " + freezerTemperatureC);

        return Optional.empty();
    }
}
