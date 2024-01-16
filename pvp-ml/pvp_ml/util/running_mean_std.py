import logging

import torch as th

logger = logging.getLogger(__name__)

# Can be useful for detecting input issues, but adds a little more processing + can pollute logs
_WARN_ON_CLAMP = False


# General idea from SB3's RunningMeanStd
class TensorRunningMeanStd:
    def __init__(
        self,
        shape: tuple[int, ...],
        count_eps: float = 1e-4,
        norm_eps: float = 1e-8,
        clip_upper: float = 5,
        clip_lower: float = -5,
        dtype: th.dtype = th.float32,
        device: str = "cpu",
    ):
        self.shape = shape
        self.mean = th.zeros(shape, dtype=dtype, device=device)
        self.var = th.ones(shape, dtype=dtype, device=device)
        self.count = count_eps
        self.epsilon = norm_eps
        self.clip_upper = clip_upper
        self.clip_lower = clip_lower

    def to(self, device: str) -> None:
        self.mean = self.mean.to(device=device)
        self.var = self.var.to(device=device)

    def normalize(
        self,
        input_arr: th.Tensor,
        apply_mean: bool = True,
        apply_std: bool = True,
        clip: bool = False,
    ) -> th.Tensor:
        # Select first 'n' features if not all given (for ex. could be skipping extra critic obs)
        arr = input_arr
        mean = self.mean[..., : arr.shape[-1]]
        var = self.var[..., : arr.shape[-1]]
        if apply_mean:
            arr = arr - mean
        if apply_std:
            arr = arr / th.sqrt(var + self.epsilon)
        if clip:
            arr = th.clamp(arr, self.clip_lower, self.clip_upper)
            self._check_clamps(input_arr, arr)
        return arr

    def update(self, arr: th.Tensor) -> None:
        assert len(arr.shape) == 2
        batch_mean = th.mean(arr, dim=0)
        batch_var = th.var(arr, dim=0, unbiased=False)
        batch_count = arr.shape[0]
        self._update_from_moments(batch_mean, batch_var, batch_count)

    def _update_from_moments(
        self, batch_mean: th.Tensor, batch_var: th.Tensor, batch_count: int
    ) -> None:
        delta = batch_mean - self.mean
        tot_count = self.count + batch_count

        new_mean = self.mean + delta * batch_count / tot_count
        m_a = self.var * self.count
        m_b = batch_var * batch_count
        m_2 = (
            m_a
            + m_b
            + th.square(delta) * self.count * batch_count / (self.count + batch_count)
        )
        new_var = m_2 / (self.count + batch_count)

        new_count = batch_count + self.count

        self.mean = new_mean
        self.var = new_var
        self.count = new_count

    def _check_clamps(
        self, initial_input_arr: th.Tensor, clamped_arr: th.Tensor
    ) -> None:
        if _WARN_ON_CLAMP:
            is_clamped_indices = (clamped_arr == self.clip_lower) | (
                clamped_arr == self.clip_upper
            )
            if th.any(is_clamped_indices):
                clamped_indices = is_clamped_indices.nonzero(as_tuple=True)
                transposed_indices = list(zip(*clamped_indices))
                clamped_values = initial_input_arr[is_clamped_indices]
                warning_message = (
                    f"Clamped {th.sum(is_clamped_indices)} input values:\n"
                )
                clamped_messages = []
                for index, value in zip(transposed_indices, clamped_values):
                    formatted_index = ", ".join(str(idx.item()) for idx in index)
                    clamped_messages.append(
                        f"Index: [{formatted_index}], Value: {value.item()}"
                    )
                warning_message += "\n".join(clamped_messages)
                logger.warning(warning_message)
