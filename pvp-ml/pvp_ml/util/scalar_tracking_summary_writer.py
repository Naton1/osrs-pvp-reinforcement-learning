from typing import Any

from torch.utils.tensorboard import SummaryWriter

from pvp_ml.ppo.ppo import PPO


# Custom SummaryWriter to delegate scalars to custom data field
# and optionally avoid histograms (huge performance hit)
class ScalarTrackingSummaryWriter(SummaryWriter):
    def __init__(
        self, *args: Any, enable_tracking_histograms: bool = True, **kwargs: Any
    ):
        super().__init__(*args, **kwargs)
        self._ppo: PPO | None = None
        self._enable_histograms = enable_tracking_histograms

    def set_model(self, ppo: PPO) -> None:
        self._ppo = ppo

    def add_scalar(
        self, tag: str, scalar_value: Any, *args: Any, **kwargs: Any
    ) -> None:
        super().add_scalar(tag, scalar_value, *args, **kwargs)
        if self._ppo is not None:
            stats = self._ppo.meta.custom_data.setdefault("stats", {})
            stats[tag] = scalar_value

    def add_histogram(
        self,
        *args: Any,
        **kwargs: Any,
    ) -> None:
        if self._enable_histograms:
            super().add_histogram(*args, **kwargs)
