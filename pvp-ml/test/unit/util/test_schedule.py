import pytest

from pvp_ml.util.schedule import ConstantSchedule, ExpressionSchedule, schedule


@pytest.mark.parametrize("timestep", [0, 10, 100])
def test_constant_schedule(timestep: int) -> None:
    assert ConstantSchedule(15).value(timestep) == 15


@pytest.mark.parametrize("timestep", [0, 10, 100])
def test_parse_constant_schedule(timestep: int) -> None:
    assert schedule("15").value(timestep) == 15


def test_expression_schedule() -> None:
    # 2t > 50 + y
    expression_schedule = ExpressionSchedule[bool](
        expression="t + var > 50 + y",
        substitutions={
            "var": ExpressionSchedule[float](
                expression="t",
            )
        },
        defaults={
            "y": 0,
        },
    )
    assert not expression_schedule.value(25)
    assert not expression_schedule.value(26, y=5)
    assert expression_schedule.value(26)
    assert expression_schedule.value(100, y=5)
