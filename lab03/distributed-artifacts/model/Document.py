class Document:
    def __init__(self, name: str, content: str) -> None:
        assert (
            name is not None and content is not None
        ), "Document constructor arguments cannot be `None`"
        self.name = name
        self.content = content