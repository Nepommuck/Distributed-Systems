package server.model;


import grpc.gen.Device.DeviceType;
import grpc.gen.Device.PowerState;
import grpc.gen.Lamp.Brightness;
import grpc.gen.Lamp.LampInfo;
import grpc.gen.Lamp.LampType;

public record Lamp(
        int id,
        PowerState powerState,
        LampType lampType,
        Float brightnessLevel,
        Float temperatureK,
        Float secondsSinceLastMovement
) implements DeviceSubtype<LampInfo> {
    @Override
    public Device asDevice() {
        return new Device(id, DeviceType.LAMP, powerState);
    }

    @Override
    public LampInfo asGrpc() {
        var brightness = Brightness.newBuilder();
        if (brightnessLevel != null) {
            brightness.setBrightnessLevel(brightnessLevel);
        }
        if (temperatureK != null) {
            brightness.setTemperatureK(temperatureK);
        }

        var result = LampInfo.newBuilder()
                .setDeviceInfo(this.asDevice().asGrpc())
                .setLampType(lampType)
                .setBrightness(brightness);

        if (secondsSinceLastMovement != null) {
            result.setSecondsSinceLastMovement(secondsSinceLastMovement);
        }

        return result.build();
    }
}
