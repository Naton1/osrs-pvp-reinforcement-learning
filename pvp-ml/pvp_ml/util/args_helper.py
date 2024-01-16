import json
from typing import Any


def replace_dash_with_underscore(d: dict[str, Any]) -> dict[str, Any]:
    # Replace all '-' with '_' in the keys (CLI args naturally use - and not _)
    new_dict = {}
    for key, value in d.items():
        new_key = key.replace("-", "_")
        if isinstance(value, dict):
            new_dict[new_key] = replace_dash_with_underscore(value)
        else:
            new_dict[new_key] = value
    return new_dict


def union_int_or_int_list(input_str: str) -> int | list[int]:
    # Try raw int
    try:
        return int(input_str)
    except ValueError:
        pass
    # Try json int list
    try:
        json_input = json.loads(input_str)
        if isinstance(json_input, list):
            assert all(isinstance(i, int) for i in json_input)
            return json_input
    except ValueError:
        pass
    # Try comma separated int list
    try:
        return [int(item) for item in input_str.split(",")]
    except ValueError:
        pass
    raise ValueError(f"Unable to convert {input_str} into int or int list")


def strtobool(val: str) -> bool:
    # Taken from python's distlib.util.strtobool (which is deprecated)
    val = val.lower()
    if val in ("y", "yes", "t", "true", "on", "1"):
        return True
    elif val in ("n", "no", "f", "false", "off", "0"):
        return False
    else:
        raise ValueError("invalid truth value %r" % (val,))
