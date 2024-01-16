import asyncio
from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from test.integ.api_client import ApiClient
from typing import Any

import pytest

from pvp_ml.api import Request, run_api
from pvp_ml.util.contract_loader import load_environment_contract
from pvp_ml.util.remote_processor.remote_processor import (
    RAY_REMOTE_PROCESSOR,
    REMOTE_PROCESSOR_TYPES,
    THREAD_REMOTE_PROCESSOR,
)
from pvp_ml.util.socket_helper import is_port_taken_async


@asynccontextmanager
async def api(
    host: str = "localhost",
    port: int = 9999,
    remote_processor_type: str = THREAD_REMOTE_PROCESSOR,
    remote_processor_kwargs: dict[str, Any] = {},
) -> AsyncIterator[ApiClient]:
    # Start API
    api_runner = run_api(
        host=host,
        port=port,
        remote_processor_pool_size=1,
        remote_processor_type=remote_processor_type,
        remote_processor_kwargs=remote_processor_kwargs,
        device="cpu",
    )
    api_runner_task = asyncio.create_task(api_runner)
    # Wait to initialize
    attempts = 0
    while not await is_port_taken_async(port):
        await asyncio.sleep(1)
        attempts += 1
        assert attempts < 50, f"Server failed to start {host}:{port}"
    try:
        async with ApiClient(host=host, port=port) as pvp_client:
            # Give control back to caller
            yield pvp_client
    finally:
        # Clean up
        api_runner_task.cancel()
        try:
            await api_runner
        except Exception:
            pass


@pytest.mark.parametrize("remote_processor_type", REMOTE_PROCESSOR_TYPES)
async def test_api_model_prediction(remote_processor_type: str) -> None:
    nh_env = load_environment_contract("NhEnv")
    remote_processor_kwargs = {}
    if remote_processor_type == RAY_REMOTE_PROCESSOR:
        # In case the machine doesn't have enough CPUs, override, so we can spin up 1 processor
        remote_processor_kwargs["cpus_per_actor"] = 0
    async with api(
        remote_processor_type=remote_processor_type,
        remote_processor_kwargs=remote_processor_kwargs,
    ) as client:
        action_masks = [
            [True] * len(action_head.actions) for action_head in nh_env.actions
        ]
        obs_space = nh_env.get_observation_space()
        obs_space.seed(1)
        observations = [obs_space.sample().tolist()]

        response = await client.send_request(
            Request(
                model="GeneralizedNh",
                actionMasks=action_masks,
                obs=observations,
                deterministic=False,
                returnLogProb=True,
                returnEntropy=True,
                returnValue=False,
                returnProbs=True,
                extensions=["winrate"],
            )
        )

    assert response is not None
    assert len(response.action) == len(nh_env.actions)
    assert len(response.extensionResults) == 1
    assert response.values is None
    assert response.logProb is not None
    assert response.probs is not None
    assert len(response.probs) == len(nh_env.actions)
    assert response.entropy is not None


async def test_api_plugin_prediction() -> None:
    nh_env = load_environment_contract("NhEnv")
    async with api() as client:
        action_masks = [
            [True] * len(action_head.actions) for action_head in nh_env.actions
        ]
        obs_space = nh_env.get_observation_space()
        obs_space.seed(1)
        observations = [obs_space.sample().tolist()]

        response = await client.send_request(
            Request(
                model="noop",
                actionMasks=action_masks,
                obs=observations,
            )
        )

    assert response is not None
    assert len(response.action) == len(nh_env.actions)
    assert len(response.extensionResults) == 0
    assert response.values is None
    assert response.logProb is None
    assert response.probs is None
    assert response.entropy is None
