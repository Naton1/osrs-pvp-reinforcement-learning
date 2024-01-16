import dataclasses
from json import JSONEncoder
from typing import Any


class GeneralizedObjectEncoder(JSONEncoder):
    def default(self, o: Any) -> Any:
        if dataclasses.is_dataclass(o):
            return dataclasses.asdict(o)
        if hasattr(o, "__dict__") and o.__dict__:
            return o.__dict__
        return str(o)
