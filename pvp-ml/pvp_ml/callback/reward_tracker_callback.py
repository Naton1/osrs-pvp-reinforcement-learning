from pvp_ml.callback.callback import Callback
from pvp_ml.ppo.buffer import Buffer


class RewardTrackerCallback(Callback):
    def on_rollout_end(self, buffer: Buffer) -> None:
        assert (
            self._summary_writer is not None
        ), "Summary writer is required to track metrics"
        assert self._ppo is not None
        counts: dict[str, int] = {}
        sums: dict[str, float] = {}
        for env_infos in buffer.infos:
            for step_info in env_infos:
                if "rewards" not in step_info:
                    continue
                for key, value in step_info["rewards"].items():
                    counts[key] = counts.get(key, 0) + 1
                    sums[key] = sums.get(key, 0) + value
        overall_total = 0.0
        for key in counts.keys():
            count = counts[key]
            total = sums[key]
            avg = total / count
            overall_total += total
            self._summary_writer.add_scalar(
                f"rewards/{key}_count", count, self._ppo.meta.trained_steps
            )
            self._summary_writer.add_scalar(
                f"rewards/{key}_total", total, self._ppo.meta.trained_steps
            )
            self._summary_writer.add_scalar(
                f"rewards/{key}_avg", avg, self._ppo.meta.trained_steps
            )
        self._summary_writer.add_scalar(
            "rewards/overall_total", overall_total, self._ppo.meta.trained_steps
        )
