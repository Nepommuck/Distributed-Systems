package server.mock;

import grpc.gen.Device.PowerState;
import server.model.Device;
import server.model.DeviceSubtype;
import server.model.exception.NotFoundException;

import java.util.Optional;
import java.util.stream.Stream;

public class DeviceMockService {
    private final LampMockDao lampDao;
    private final FridgeMockDao fridgeDao;

    public DeviceMockService(LampMockDao lampDao, FridgeMockDao fridgeDao) {
        this.lampDao = lampDao;
        this.fridgeDao = fridgeDao;
    }

    public Stream<Device> getAll() {
        return Stream.concat(
                lampDao.getAll(),
                fridgeDao.getAll()
        ).map(DeviceSubtype::asDevice);
    }

    public Optional<Device> getById(int id) {
        return this.getAll()
                .filter(device -> device.id() == id)
                .findFirst();
    }

    public void changePowerState(
            int deviceId, PowerState newState
    ) throws NotFoundException, IllegalStateException, InternalError {
        try {
            final var foundDevice = this.getById(deviceId)
                    .orElseThrow(() -> new NotFoundException("No device found with id: " + deviceId));

            switch (foundDevice.type()) {
                case LAMP -> lampDao.changePowerState(deviceId, newState);
                case FRIDGE -> fridgeDao.changePowerState(deviceId, newState);
                default -> throw new InternalError("Device type not supported");
            }
        } catch (NotFoundException e) {
            throw new IllegalStateException("Device present in `DeviceService`, but not found in database", e);
        }
    }
}
