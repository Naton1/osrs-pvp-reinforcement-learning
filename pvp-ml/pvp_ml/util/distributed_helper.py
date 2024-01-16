from collections.abc import Callable
from typing import TypeVar

from pvp_ml.ppo.ppo import Meta

T = TypeVar("T")


def merge_meta_values(
    metas: list[Meta],
    meta_key: str,
    combined_meta: Meta,
    merge_fn: Callable[[list[T]], T],
) -> None:
    # Useful to aggregate custom data stored in the model meta across distributed rollouts
    values = []
    for meta in metas:
        if meta_key in meta.custom_data:
            value = meta.custom_data[meta_key]
            values.append(value)
    if values:
        merged_value = merge_fn(values)
        combined_meta.custom_data[meta_key] = merged_value
