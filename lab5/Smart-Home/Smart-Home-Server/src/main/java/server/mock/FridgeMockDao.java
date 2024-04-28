package server.mock;

import grpc.gen.Device.PowerState;
import server.model.Fridge;
import server.model.exception.NotFoundException;
import server.model.exception.StateAlreadySetException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FridgeMockDao {
    private final List<Fridge> state = new ArrayList<>(InitialState.fridgeInitialState);

    public Stream<Fridge> getAll() {
        return state.stream();
    }

    public Optional<Fridge> getById(int deviceId) throws NotFoundException {
        return state.stream()
                .filter(fridge -> fridge.id() == deviceId)
                .findFirst();
    }

    public void setDesiredTemperature(
            int deviceId,
            float newDesiredFridgeTemperatureC,
            float newDesiredFreezerTemperatureC
    ) throws NotFoundException {
        final var foundFridge = this.getById(deviceId)
                .orElseThrow(() -> new NotFoundException("No fridge found with id: " + deviceId));

        state.remove(foundFridge);
        state.add(new Fridge(
                deviceId,
                foundFridge.powerState(),
                newDesiredFridgeTemperatureC,
                newDesiredFreezerTemperatureC
        ));
    }

    public void changePowerState(
            int deviceId, PowerState newState
    ) throws NotFoundException, StateAlreadySetException {
        final var foundFridge = this.getById(deviceId)
                .orElseThrow(() -> new NotFoundException("No fridge found with id: " + deviceId));

        if (foundFridge.powerState() == newState) {
            throw new StateAlreadySetException("Fridge with id " + deviceId + " has already state " + newState);
        }

        final var turnOff = newState.equals(PowerState.OFF);
        final var desiredFridgeTemperatureC = turnOff ? null : foundFridge.desiredFridgeTemperatureC();
        final var desiredFreezerTemperatureC = turnOff ? null : foundFridge.desiredFreezerTemperatureC();

        state.remove(foundFridge);
        state.add(new Fridge(
                deviceId,
                foundFridge.powerState(),
                desiredFridgeTemperatureC,
                desiredFreezerTemperatureC
        ));
    }
}

