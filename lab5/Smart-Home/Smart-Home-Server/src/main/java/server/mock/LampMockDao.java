package server.mock;

import grpc.gen.Device.PowerState;
import server.model.Lamp;
import server.model.exception.NotFoundException;
import server.model.exception.StateAlreadySetException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class LampMockDao {
    private final List<Lamp> state = new ArrayList<>(InitialState.lampsInitialState);

    public Stream<Lamp> getAll() {
        return state.stream();
    }

    public Optional<Lamp> getById(int deviceId) throws NotFoundException {
        return state.stream()
                .filter(lamp -> lamp.id() == deviceId)
                .findFirst();
    }

    public void setBrightness(int deviceId, Float newBrightnessLevel, Float newTemperatureK) throws NotFoundException {
        final var foundLamp = this.getById(deviceId)
                .orElseThrow(() -> new NotFoundException("No lamp found with id: " + deviceId));

        state.remove(foundLamp);
        state.add(new Lamp(
                deviceId,
                foundLamp.powerState(),
                foundLamp.lampType(),
                newBrightnessLevel,
                newTemperatureK,
                foundLamp.secondsSinceLastMovement()
        ));
    }

    public void changePowerState(
            int deviceId, PowerState newState
    ) throws NotFoundException, StateAlreadySetException {
        final var foundLamp = this.getById(deviceId)
                .orElseThrow(() -> new NotFoundException("No lamp found with id: " + deviceId));

        if (foundLamp.powerState() == newState) {
            throw new StateAlreadySetException("Lamp with id " + deviceId + " has already state " + newState);
        }

        final var turnOff = newState.equals(PowerState.OFF);
        final var brightnessLevel = turnOff ? null : foundLamp.brightnessLevel();
        final var temperatureK = turnOff ? null : foundLamp.temperatureK();
        final var secondsSinceLastMovement = turnOff ? null : foundLamp.secondsSinceLastMovement();

        state.remove(foundLamp);
        state.add(new Lamp(
                deviceId,
                newState,
                foundLamp.lampType(),
                brightnessLevel,
                temperatureK,
                secondsSinceLastMovement
        ));
    }
}
