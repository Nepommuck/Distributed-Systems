class Command:
    def __init__(
        self, name: str, argument_count: int, usage: str, description: str
    ) -> None:
        self.name = name
        self.argument_count = argument_count
        self.usage = usage
        self.description = description

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
    help = Command(
        name="help",
        argument_count=0,
        usage="help",
        description="Display help",
    )
    upload = Command(
        name="upload",
        argument_count=2,
        usage="upload ARTIFACT_NAME CONTENT",
        description="Upload new artifact",
    )
    read = Command(
        name="read",
        argument_count=1,
        usage="read ARTIFACT_NAME",
        description="Read the content of an existing artifact",
    )
    modify = Command(
        name="modify",
        argument_count=2,
        usage="modify ARTIFACT_NAME NEW_CONTENT",
        description="Modify the content of an existing artifact",
    )
    delete = Command(
        name="delete",
        argument_count=1,
        usage="delete ARTIFACT_NAME",
        description="Delete an existing artifact",
    )
    status = Command(
        name="status",
        argument_count=0,
        usage="status",
        description="Inspect cluster status",
    )
    exit = Command(
        name="exit",
        argument_count=0,
        usage="exit",
        description="Shutdown the server",
    )

    all = [help, upload, read, modify, delete, status, exit]
