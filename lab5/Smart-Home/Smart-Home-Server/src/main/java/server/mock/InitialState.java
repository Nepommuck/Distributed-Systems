package server.mock;

import grpc.gen.Device.PowerState;
import grpc.gen.Lamp.LampType;
import server.model.Device;
import server.model.Fridge;
import server.model.Lamp;

import java.util.List;

interface InitialState {
    List<Lamp> lampsInitialState = List.of(
            new Lamp(1, PowerState.ON, LampType.STATIC_LAMP, 80f, 5_000f, null),
            new Lamp(2, PowerState.ON, LampType.STATIC_LAMP, 20f, 1_700f, null),
            new Lamp(3, PowerState.OFF, LampType.STATIC_LAMP, null, null, null),

            new Lamp(4, PowerState.ON, LampType.MOVE_DETECTION, null, null, 17f),
            new Lamp(5, PowerState.OFF, LampType.MOVE_DETECTION, null, null, null)
    );

    List<Fridge> fridgeInitialState = List.of(
            new Fridge(10, PowerState.ON, 8f, -10f),
            new Fridge(11, PowerState.ON, 3f, -50f),
            new Fridge(12, PowerState.OFF, null, null)
    );

    List<Device> initialStateDevice = lampsInitialState.stream().map(Lamp::asDevice).toList();
}
