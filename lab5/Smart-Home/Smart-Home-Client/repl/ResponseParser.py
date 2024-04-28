from proto.Device_pb2 import (
    DeviceInfo,
    DeviceType,
    PowerState,
    GetDevicesResponse,
    Empty,
)
from proto.Lamp_pb2 import LampInfo, LampType
from proto.Fridge_pb2 import FridgeInfo


class ResponseParser:
    def parse_empty(empty: Empty):
        return f"Empty response"

    def parse_device_info(device_info: DeviceInfo):
        return (
            f"Device {device_info.id.value}\n"
            + f"Type:        {DeviceType.Name(device_info.type)}\n"
            + f"Power state: {PowerState.Name(device_info.powerState)}"
        )

    def parse_get_devices_response(get_devices_response: GetDevicesResponse):
        return "\n\n".join(
            [
                ResponseParser.parse_device_info(dev_info)
                for dev_info in get_devices_response.devices
            ]
        )

    def parse_lamp_info(lamp_info: LampInfo):
        return (
            f"Lamp {lamp_info.deviceInfo.id.value}\n"
            + f"Power state:       {PowerState.Name(lamp_info.deviceInfo.powerState)}\n"
            + f"Lamp Type:         {LampType.Name(lamp_info.lampType)}\n"
            + f"Brightness level:  {lamp_info.brightness.brightnessLevel}\n"
            + f"Temperature [K]:   {int(lamp_info.brightness.temperatureK)}\n"
            + f"Last movement [s]: {lamp_info.secondsSinceLastMovement:.2f}"
        )

    def parse_fridge_info(fridge_info: FridgeInfo):
        return (
            f"Fridge {fridge_info.deviceInfo.id.value}\n"
            + f"Power state:                      {PowerState.Name(fridge_info.deviceInfo.powerState)}\n"
            + f"Desired fridge temperature [°C]:  {fridge_info.desiredTemperature.fridgeTemperatureC:.2f}\n"
            + f"Actual fridge temperature [°C]:   {fridge_info.actualTemperature.fridgeTemperatureC:.2f}\n"
            + f"Desired freezer temperature [°C]: {fridge_info.desiredTemperature.freezerTemperatureC:.2f}\n"
            + f"Actual freezer temperature [°C]:  {fridge_info.actualTemperature.freezerTemperatureC:.2f}"
        )
