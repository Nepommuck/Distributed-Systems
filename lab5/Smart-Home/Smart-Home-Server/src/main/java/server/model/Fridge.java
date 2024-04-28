package server.model;

import grpc.gen.Device.DeviceType;
import grpc.gen.Device.PowerState;
import grpc.gen.Fridge.FridgeInfo;
import grpc.gen.Fridge.Temperature;

import java.util.Random;

public record Fridge(
        int id,
        PowerState powerState,
        Float desiredFridgeTemperatureC,
        Float desiredFreezerTemperatureC
) implements DeviceSubtype<FridgeInfo> {

    @Override
    public Device asDevice() {
        return new Device(id, DeviceType.FRIDGE, powerState);
    }

    @Override
    public FridgeInfo asGrpc() {
        final var builder = FridgeInfo.newBuilder()
                .setDeviceInfo(this.asDevice().asGrpc());

        if (powerState.equals(PowerState.ON)) {
            builder.setDesiredTemperature(Temperature.newBuilder()
                            .setFridgeTemperatureC(desiredFridgeTemperatureC)
                            .setFreezerTemperatureC(desiredFreezerTemperatureC)
                    ).setActualTemperature(this.getActualTemperature())
                    .build();
        }
        return builder.build();
    }

    private Temperature getActualTemperature() {
        Random random = new Random();
        final var stDev = 1.5;
        final var fridgeTemperatureC = (float) random.nextGaussian(desiredFridgeTemperatureC + stDev, stDev);
        final var freezerTemperatureC = (float) random.nextGaussian(desiredFreezerTemperatureC + stDev, stDev);

        return Temperature.newBuilder()
                .setFridgeTemperatureC(fridgeTemperatureC)
                .setFreezerTemperatureC(freezerTemperatureC)
                .build();
    }
}
