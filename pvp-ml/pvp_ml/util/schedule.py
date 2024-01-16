import abc
import ast
import json
import logging
import math
import operator
from collections.abc import Callable
from typing import Any, Generic, TypeVar, cast

import numpy as np
from numpy.typing import NDArray

from pvp_ml.util.args_helper import replace_dash_with_underscore

logger = logging.getLogger(__name__)
T = TypeVar("T")


class Schedule(abc.ABC, Generic[T]):
    @abc.abstractmethod
    def value(self, t: float, **kwargs: Any) -> T:
        pass


class ConstantSchedule(Schedule[T]):
    def __init__(self, value: T):
        self._value = value

    def value(self, t: float, **kwargs: Any) -> T:
        return self._value

    def __str__(self) -> str:
        return f"{self._value}"


class JumpSchedule(Schedule[T]):
    def __init__(self, jumps: dict[str, Schedule[T]]):
        self._jumps = {float(key): value for key, value in jumps.items()}

    def value(self, t: float, **kwargs: Any) -> T:
        best_jump = None
        for threshold, jump_schedule in self._jumps.items():
            if threshold > t:
                break
            best_jump = jump_schedule
        if best_jump is None:
            raise ValueError(t)
        return best_jump.value(t)

    def __str__(self) -> str:
        return f"Jump up: {self._jumps.keys()}"


class CallableSchedule(Schedule[T]):
    def __init__(self, func: Callable[[float], T]):
        self._func = func

    def value(self, t: float, **kwargs: Any) -> T:
        return self._func(t)

    def __str__(self) -> str:
        return f"{self._func.__name__}(t)"


class LinearSchedule(Schedule[float]):
    def __init__(
        self,
        initial_value: float,
        final_value: float,
        change_over_time_steps: float,
        round_value: bool = False,
        round_digits: int | None = None,
    ):
        self._initial_value = initial_value
        self._final_value = final_value
        self._change_over_time_steps = change_over_time_steps
        self._round_value = round_value
        self._round_digits = round_digits

    def value(self, t: float, **kwargs: Any) -> float:
        progress = min(1.0, t / self._change_over_time_steps)
        value = (
            self._initial_value + (self._final_value - self._initial_value) * progress
        )
        if self._round_value:
            value = round(value, ndigits=self._round_digits)
        return value

    def __str__(self) -> str:
        return f"{self._initial_value} -> {self._final_value} over {self._change_over_time_steps} steps"


class LogSchedule(Schedule[float]):
    def __init__(
        self,
        initial_value: float,
        final_value: float,
        change_over_time_steps: float,
        base: float = 10.0,
    ):
        self._initial_value = initial_value
        self._final_value = final_value
        self._change_over_time_steps = change_over_time_steps
        self._base = base

    def value(self, t: float, **kwargs: Any) -> float:
        progress = min(1.0, t / self._change_over_time_steps)
        log_initial_value = cast(
            float, (np.log(self._initial_value) / np.log(self._base)).item()
        )
        log_final_value = cast(
            float, (np.log(self._final_value) / np.log(self._base)).item()
        )
        log_value = log_initial_value + (log_final_value - log_initial_value) * progress
        return cast(float, self._base**log_value)

    def __str__(self) -> str:
        return f"{self._initial_value} -> {self._final_value} over {self._change_over_time_steps} steps (log space, base {self._base})"


class ExpressionSchedule(Schedule[T]):
    _operators: dict[type[ast.operator | ast.unaryop], Callable[..., Any]] = {
        ast.Add: operator.add,
        ast.Sub: operator.sub,
        ast.Mult: operator.mul,
        ast.Div: operator.truediv,
        ast.USub: operator.neg,
        ast.Pow: operator.pow,
    }

    _functions: dict[str, Callable[..., Any]] = {
        "min": min,
        "max": max,
        "round": round,
        "abs": abs,
        "random": np.random.random,
        "normal": np.random.normal,
        "sin": math.sin,
        "cos": math.cos,
        "log": math.log,
        "log10": math.log10,
        "log2": math.log2,
    }

    _comparators: dict[type[ast.cmpop], Callable[..., Any]] = {
        ast.Eq: operator.eq,
        ast.NotEq: operator.ne,
        ast.Lt: operator.lt,
        ast.LtE: operator.le,
        ast.Gt: operator.gt,
        ast.GtE: operator.ge,
    }

    _depth_limit = 100
    _depth_keyword = "__depth"

    def __init__(
        self,
        expression: str,
        substitutions: dict[str, Schedule[Any]] = {},
        defaults: dict[str, Any] = {},
    ):
        self._expression = expression
        self._substitutions = substitutions
        self._defaults = defaults

    def value(self, t: float, **kwargs: Any) -> T:
        variables: dict[str, Any] = {
            substitution_key: substitution_schedule.value(t)
            for substitution_key, substitution_schedule in self._substitutions.items()
        }
        for variable_mapping in [kwargs, self._defaults]:
            for key, value in variable_mapping.items():
                if key not in variables:
                    variables[key] = value
        variables["t"] = t
        root = ast.parse(self._expression, mode="eval").body
        return cast(T, self._eval(root, variables))

    def _eval(self, node: Any, variables: dict[str, Any]) -> Any:
        variables[self._depth_keyword] = variables.get(self._depth_keyword, 0) + 1
        if variables[self._depth_keyword] > self._depth_limit:
            # Let's limit the complexity. No reason this should ever need too many recursive calls.
            raise ValueError(self._expression)
        elif isinstance(node, ast.Num):  # <number>
            return node.n
        elif isinstance(node, ast.Str):  # <string>
            return node.n
        elif isinstance(node, ast.Constant):  # <constant> (string)
            return node.n
        elif isinstance(node, ast.Name):  # <variable>
            return variables[node.id]
        elif isinstance(node, ast.BinOp):  # <left> <operator> <right>
            return self._operators[type(node.op)](
                self._eval(node.left, variables), self._eval(node.right, variables)
            )
        elif isinstance(node, ast.UnaryOp):  # <operator> <operand>
            return self._operators[type(node.op)](self._eval(node.operand, variables))
        elif isinstance(node, ast.Call):  # <function>(<args>)
            assert isinstance(node.func, ast.Name)
            func = self._functions[node.func.id]
            args = [self._eval(arg, variables) for arg in node.args]
            return func(*args)
        elif isinstance(node, ast.Compare):  # <left> <comparator> <comparators>
            left = self._eval(node.left, variables)
            result = True
            for comparator, comparand in zip(node.ops, node.comparators):
                result = result and self._comparators[type(comparator)](
                    left, self._eval(comparand, variables)
                )
                left = self._eval(comparand, variables)
            return result
        elif isinstance(node, ast.IfExp):  # <body> if <test> else <orelse>
            return (
                self._eval(node.body, variables)
                if self._eval(node.test, variables)
                else self._eval(node.orelse, variables)
            )
        else:
            raise TypeError(node)

    def __str__(self) -> str:
        return self._expression


class NpBoolArraySchedule(Schedule[NDArray[np.bool_]]):
    # Intended for action masks
    def __init__(
        self,
        size: int = 0,
        true_indices: list[int] = [],
        false_indices: list[int] = [],
        default_value: bool = True,
    ):
        self._array = np.full(shape=(size,), fill_value=default_value, dtype=bool)
        self._array[true_indices] = True
        self._array[false_indices] = False

    def value(self, t: float, **kwargs: Any) -> NDArray[np.bool_]:
        return self._array

    def __str__(self) -> str:
        return f"{self._array}"


def schedule(arg: str) -> Schedule[Any]:
    """
    Utility function to parse a json config into a Schedule
    :param arg: the json config
    :return: a schedule based on the json config
    """
    try:
        schedule_json = json.loads(arg)

        if not isinstance(schedule_json, dict):
            return ConstantSchedule(schedule_json)

        schedule_types: dict[str, Callable[..., Schedule[Any]]] = {
            "constant": ConstantSchedule,
            "linear": LinearSchedule,
            "log": LogSchedule,
            "jump": JumpSchedule,
            "npboolarray": NpBoolArraySchedule,
            "expression": ExpressionSchedule,
        }

        schedule_type = schedule_json.get("type")

        if schedule_type not in schedule_types:
            raise ValueError(f"Unknown schedule type: {schedule_type}")

        del schedule_json["type"]

        schedule_json = replace_dash_with_underscore(schedule_json)

        if schedule_type == "jump":
            # Special logic to parse nested schedules
            for key, value in schedule_json["jumps"].items():
                schedule_json["jumps"][key] = schedule(json.dumps(value))
        elif schedule_type == "expression":
            # Special logic to parse expression schedules
            if "substitutions" in schedule_json:
                for key, value in schedule_json["substitutions"].items():
                    schedule_json["substitutions"][key] = schedule(json.dumps(value))

        generated_schedule = schedule_types[schedule_type](**schedule_json)
        # Let's validate that the config is good enough to get a value
        generated_schedule.value(0)
        return generated_schedule
    except Exception:
        # Print exception because argparse will not
        logger.exception(f"Invalid schedule: {arg}")
        raise
