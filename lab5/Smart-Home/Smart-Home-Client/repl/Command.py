class Command:
    def __init__(
        self, name: str, argument_count: int, usage: str, description: str, parse_arguments_fun = None,
    ) -> None:
        self.name = name
        self.argument_count = argument_count
        self.usage = usage
        self.description = description
        self.parse_arguments_fun = parse_arguments_fun

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Command):
            return False
        return (
            self.name == other.name
            and self.argument_count == other.argument_count
            and self.usage == other.usage
            and self.description == other.description
        )
    

class AvailableCommands:
    def __parse_device_id(args) -> tuple[tuple[int], str]:
        try:
            return (int(args[0]),), None
        except ValueError:
            error = f"DEVICE_ID must be an integer, but was '{args[0]}'"
            return None, error
        
    def __parse_brightness(args) -> tuple[tuple[int, float, float], str]:
        [device_id], error = AvailableCommands.__parse_device_id(args[0])
        if error is not None:
            return None, error
        
        try:
            brightness_level = float(args[1])
        except ValueError:
            error = f"BRIGHTNESS_LEVEL must be a float, but was '{args[1]}'"
            return None, error
        try:
            temperature_k = float(args[2])
        except ValueError:
            error = f"TEMPERATURE_K must be a float, but was '{args[2]}'"
            return None, error
        
        return (device_id, brightness_level, temperature_k), None
        
    def __parse_temperature(args) -> tuple[tuple[int, float, float], str]:
        [device_id], error = AvailableCommands.__parse_device_id([args[0]])
        if error is not None:
            return None, error
        
        try:
            fridge_temperature = float(args[1])
        except ValueError:
            error = f"FRIDGE_TEMPERATURE must be a float, but was '{args[1]}'"
            return None, error
        try:
            freezer_temperature = float(args[2])
        except ValueError:
            error = f"FREEZER_TEMPERATURE must be a float, but was '{args[2]}'"
            return None, error
        
        return (device_id, fridge_temperature, freezer_temperature), None
        
    help = Command(
        name="help",
        argument_count=0,
        usage="help",
        description="Display help",
    )

    get_all_devices = Command(
        name="get-all",
        argument_count=0,
        usage="get-all",
        description="Get all devices",
    )

    get_device = Command(
        name="get",
        argument_count=1,
        usage="get DEVICE_ID",
        description="Get selected device details",
        parse_arguments_fun=__parse_device_id,
    )
        
    turn_on = Command(
        name="turn-on",
        argument_count=1,
        usage="turn-on DEVICE_ID",
        description="Turn on a deice",
        parse_arguments_fun=__parse_device_id,
    )

    turn_off = Command(
        name="turn-off",
        argument_count=1,
        usage="turn-off DEVICE_ID",
        description="Turn off a deice",
        parse_arguments_fun=__parse_device_id,
    )

    lamp_get = Command(
        name="lamp-get",
        argument_count=1,
        usage="lamp-get DEVICE_ID",
        description="Get lamp details",
        parse_arguments_fun=__parse_device_id,
    )

    lamp_set_brightness = Command(
        name="lamp-set",
        argument_count=3,
        usage="lamp-set DEVICE_ID BRIGHTNESS_LEVEL TEMPERATURE_K",
        description="Set lamp brightness",
        parse_arguments_fun=__parse_brightness,
    )

    fridge_get = Command(
        name="fridge-get",
        argument_count=1,
        usage="fridge-get DEVICE_ID",
        description="Get fridge details",
        parse_arguments_fun=__parse_device_id,
    )

    fridge_set_temperature = Command(
        name="fridge-set",
        argument_count=3,
        usage="fridge-set DEVICE_ID FRIDGE_TEMPERATURE FREEZER_TEMPERATURE",
        description="Set fridge desired temperature",
        parse_arguments_fun=__parse_temperature,
    )

    exit = Command(
        name="exit",
        argument_count=0,
        usage="exit",
        description="Shutdown the server",
    )

    all = [
        help, 
        get_all_devices, get_device, 
        turn_on, turn_off, 
        lamp_get, lamp_set_brightness,
        fridge_get, fridge_set_temperature,
        exit,
    ]
