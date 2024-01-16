import pytest

from pvp_ml.rate_references import main


def test_rate_references(caplog: pytest.LogCaptureFixture) -> None:
    main(
        [
            "run",
            "--num-concurrent-fights",
            "3",
            "--num-fights-per-fighter",
            "2",
            "--remote-processor-pool-size",
            "1",
            "--dry-run",
        ]
    )
    assert "Reference Ratings" in caplog.text
    assert "Updating reference ratings" not in caplog.text


def test_show(caplog: pytest.LogCaptureFixture) -> None:
    main(
        [
            "show",
        ]
    )
    assert "Reference Ratings" in caplog.text
