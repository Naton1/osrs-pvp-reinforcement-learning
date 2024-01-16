import hashlib
import logging
import os
import time
from functools import cache
from typing import Any, cast

import torch as th
from ray.actor import ActorClass

from pvp_ml.ppo.ppo import PPO
from pvp_ml.util import ray_helper
from pvp_ml.util.remote_processor.remote_processor import RemoteProcessor

logger = logging.getLogger(__name__)


class _ModelNotFoundException(ValueError):
    pass


class RayProcessor(RemoteProcessor):
    def __init__(
        self,
        pool_size: int,
        device: str = "cpu",
        max_concurrency: int = 4,
        shared: bool = True,
        cpus_per_actor: int | None = None,
    ):
        ray_helper.init()
        model_actor = self._get_model_actor_class()
        self._pool_size = pool_size
        self._device = device
        self._actor_pool = [
            model_actor.options(
                name=f"{'shared-' if shared else ''}ray-processor-{i}-{device}",
                get_if_exists=shared,
                max_concurrency=max_concurrency,
                num_cpus=cpus_per_actor
                if cpus_per_actor is not None
                else max_concurrency,
            ).remote(device)
            for i in range(pool_size)
        ]

    def get_pool_size(self) -> int:
        return self._pool_size

    def get_device(self) -> str:
        return self._device

    async def close(self) -> None:
        del self._actor_pool

    async def predict(
        self,
        process_id: int,
        model_path: str,
        observation: th.Tensor | None = None,
        action_masks: th.Tensor | None = None,
        deterministic: bool | th.Tensor = False,
        return_device: str | None = None,
        return_actions: bool = True,
        return_log_probs: bool = False,
        return_entropy: bool = False,
        return_values: bool = False,
        return_probs: bool = False,
        extensions: list[str] = [],
    ) -> tuple[
        th.Tensor | None,
        th.Tensor | None,
        th.Tensor | None,
        th.Tensor | None,
        th.Tensor | None,
        list[Any],
    ]:
        if return_device is None and observation is not None:
            return_device = str(observation.device)
        if observation is not None:
            observation = observation.cpu()
        if action_masks is not None:
            action_masks = action_masks.cpu()

        actor = self._actor_pool[process_id]
        model_hash = self._get_model_hash(model_path)

        try:
            prediction = await actor.predict.remote(
                model_hash,
                observation,
                deterministic,
                action_masks,
                return_actions=return_actions,
                return_log_probs=return_log_probs,
                return_entropy=return_entropy,
                return_values=return_values,
                return_probs=return_probs,
                extensions=extensions,
            )
        except _ModelNotFoundException:
            logger.info(
                f"Model {model_path} not found on remote, sending model with hash {model_hash}"
            )
            # Model not on remote, send model, and retry
            with open(model_path, "rb") as f:
                model_file = f.read()
            prediction = await actor.predict.remote(
                model_hash,
                observation,
                deterministic,
                action_masks,
                model_file=model_file,
                return_actions=return_actions,
                return_log_probs=return_log_probs,
                return_entropy=return_entropy,
                return_values=return_values,
                return_probs=return_probs,
                extensions=extensions,
            )

        actions, log_probs, entropy, values, probs, ext_results = prediction

        if return_device is not None:
            if actions is not None:
                actions = actions.to(return_device)
            if log_probs is not None:
                log_probs = log_probs.to(return_device)
            if entropy is not None:
                entropy = entropy.to(return_device)
            if values is not None:
                values = values.to(return_device)
            if probs is not None:
                probs = probs.to(return_device)

        return actions, log_probs, entropy, values, probs, ext_results

    @cache
    def _get_model_hash(self, file_path: str) -> str:
        sha256_hash = hashlib.sha256()
        with open(file_path, "rb") as f:
            for byte_block in iter(lambda: f.read(4096), b""):
                sha256_hash.update(byte_block)
        return sha256_hash.hexdigest()

    def _get_model_actor_class(self) -> ActorClass:
        import tempfile

        import ray

        @ray.remote
        class ModelActor:
            def __init__(
                self,
                device: str = "cpu",
                expiration_check_interval_seconds: int = 60,
                expiry_time_seconds: int = 3600,
            ):
                self._device = device
                self._model_cache: dict[str, PPO] = {}
                self._model_access_times: dict[str, float] = {}
                self._expiry_time = expiry_time_seconds
                self._last_expiration_check = 0.0
                self._expiration_check_interval_seconds = (
                    expiration_check_interval_seconds
                )

            def _load_model(
                self, model_hash: str, model_path: str | None = None
            ) -> PPO:
                self._model_access_times[model_hash] = time.time()
                if model_hash not in self._model_cache:
                    if model_path is None:
                        raise _ModelNotFoundException
                    self._model_cache[model_hash] = PPO.load(
                        model_path, device=self._device, trainable=False
                    )
                self._expire_items()
                return self._model_cache[model_hash]

            def _expire_items(self) -> None:
                current_time = time.time()
                if (
                    self._last_expiration_check
                    + self._expiration_check_interval_seconds
                    > current_time
                ):
                    # Don't check yet
                    return
                self._last_expiration_check = current_time
                model_hashes_to_expire = [
                    key
                    for key, last_access_time in self._model_access_times.items()
                    if current_time - last_access_time > self._expiry_time
                ]

                for key in model_hashes_to_expire:
                    if key in self._model_cache:
                        del self._model_cache[key]
                    del self._model_access_times[key]

            def predict(
                self,
                model_hash: str,
                observation: th.Tensor | None = None,
                deterministic: bool | th.Tensor = False,
                action_masks: th.Tensor | None = None,
                model_file: bytes | None = None,
                return_actions: bool = True,
                return_log_probs: bool = False,
                return_entropy: bool = False,
                return_values: bool = False,
                return_probs: bool = False,
                extensions: list[str] = [],
            ) -> tuple[
                th.Tensor | None,
                th.Tensor | None,
                th.Tensor | None,
                th.Tensor | None,
                th.Tensor | None,
                list[Any],
            ]:
                if model_file is not None:
                    try:
                        with tempfile.NamedTemporaryFile(mode="wb", delete=False) as f:
                            f.write(model_file)
                        model = self._load_model(model_hash, model_path=f.name)
                    finally:
                        os.unlink(f.name)
                else:
                    model = self._load_model(model_hash)

                if observation is None:
                    return None, None, None, None, None, []

                assert action_masks is not None

                return model.predict(
                    observation.to(self._device),
                    action_masks.to(self._device),
                    deterministic=deterministic,
                    return_actions=return_actions,
                    return_values=return_values,
                    return_log_probs=return_log_probs,
                    return_entropy=return_entropy,
                    return_probs=return_probs,
                    extensions=extensions,
                    return_device="cpu",
                )

        # Ray type hints are incorrect with ray.remote (and always says it's a remote function)
        return cast(ActorClass, ModelActor)
