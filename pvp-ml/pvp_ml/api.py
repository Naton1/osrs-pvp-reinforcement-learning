"""
This script will serve all models in the 'models' directory via a JSON + TCP socket-based API.
"""
import argparse
import asyncio
import dataclasses
import itertools
import json
import logging
import os
import random
import sys
import time
from asyncio import StreamReader, StreamWriter
from dataclasses import dataclass, field
from typing import Any

import torch as th

from pvp_ml.scripted.script_plugin_registry import (
    get_scripted_plugin,
    is_scripted_plugin,
)
from pvp_ml.util.args_helper import replace_dash_with_underscore
from pvp_ml.util.files import models_dir
from pvp_ml.util.remote_processor.remote_processor import (
    REMOTE_PROCESSOR_TYPES,
    THREAD_REMOTE_PROCESSOR,
    RemoteProcessor,
    create_remote_processor,
)

logger = logging.getLogger(__name__)
models = {
    os.path.splitext(filename)[0]: os.path.join(models_dir, filename)
    for filename in os.listdir(models_dir)
    if filename.endswith(".zip")
}

# Note: inputs/outputs are intentionally in camel-case here


@dataclass(frozen=True)
class Request:
    # The model name to use (can be a plugin name instead).
    model: str
    # The action masks (a list of actions for each action head).
    actionMasks: list[list[bool]]
    # The observations for the current state (a list of each frame - outer list length > 1 if frame stacking).
    obs: list[list[float | int | bool]]
    # Whether to sample deterministically. Can be a list to configure on each action head individually.
    deterministic: list[bool] | bool = False
    # This will return the log probability for taking the current action in the response.
    returnLogProb: bool = False
    # This will return the entropy for each action head distribution in the response.
    returnEntropy: bool = False
    # This will return the value estimate for the current state in the response.
    # Note: this will not work if the critic is trained on the full game state (and not partial).
    returnValue: bool = False
    # This will return the raw probabilities for each action in each action head in the response.
    returnProbs: bool = False
    # A list of model extensions to run. Results will be included in the response in the order here.
    extensions: list[str] = field(default_factory=list)


@dataclass(frozen=True)
class Response:
    # The generated action.
    action: list[int]
    # The log probability for taking the current action, if returnLogProb is True in the request.
    logProb: float | None
    # The entropy for each action head distribution if returnEntropy is True in the request.
    entropy: list[float] | None
    # The value estimate for the current state if returnValue is True in the request.
    values: list[float] | None
    # The raw probabilities for each action in each action head if returnProbs is True in the request.
    probs: list[list[float]] | None
    # The results of each model extension specified in the request, in the order the extensions were specified.
    extensionResults: list[Any]


async def handle_client(
    reader: StreamReader, writer: StreamWriter, remote_processor: RemoteProcessor
) -> None:
    client_ip, client_port, *_ = writer.get_extra_info("peername")
    client_id = f"{client_ip}:{client_port}"
    logger.info(f"[{client_id}] Client connected")
    try:
        while True:
            request_line = await reader.readline()
            if not request_line:
                break
            logger.debug(f"[{client_id}] Received request: {request_line!r}")

            request_json = json.loads(request_line)
            request = Request(**request_json)

            model_name = request.model
            is_plugin = is_scripted_plugin(model_name)
            if model_name not in models and not is_plugin:
                raise ValueError(f"Unknown model: {model_name}")

            logger.info(
                f"[{client_id}] Generating prediction using model: {model_name}"
            )
            start_time = time.time()

            # Flatten action masks, since we internally treat them as a single list
            raw_sliced_action_masks = request.actionMasks
            raw_action_masks = list(
                itertools.chain.from_iterable(raw_sliced_action_masks)
            )

            observations = th.tensor([request.obs], dtype=th.float32, device="cpu")
            action_masks = th.tensor([raw_action_masks], dtype=th.bool, device="cpu")

            return_log_prob = request.returnLogProb
            return_entropy = request.returnEntropy
            return_value = request.returnValue
            return_probs = request.returnProbs
            extensions = request.extensions
            deterministic = request.deterministic

            if not is_plugin:
                sample_deterministic: bool | th.Tensor
                if isinstance(deterministic, list):
                    sample_deterministic = th.tensor(
                        deterministic, dtype=th.bool, device="cpu"
                    )
                else:
                    sample_deterministic = deterministic

                random_pool_worker = random.randint(
                    0, remote_processor.get_pool_size() - 1
                )

                model_path = models[model_name]
                (
                    action,
                    log_probs,
                    entropy,
                    values,
                    flattened_probs,
                    ext_results,
                ) = await remote_processor.predict(
                    observation=observations,
                    deterministic=sample_deterministic,
                    action_masks=action_masks,
                    process_id=random_pool_worker,
                    model_path=model_path,
                    return_actions=True,
                    return_log_probs=return_log_prob,
                    return_entropy=return_entropy,
                    return_values=return_value,
                    return_probs=return_probs,
                    extensions=extensions,
                )
                assert action is not None
                # Convert flattened probs to action head sizes
                if return_probs:
                    assert flattened_probs is not None
                    action_head_sizes = [
                        len(action_head) for action_head in raw_sliced_action_masks
                    ]
                    cumulative_sizes = [0] + list(
                        itertools.accumulate(action_head_sizes)
                    )
                    probs = [
                        flattened_probs[
                            0, cumulative_sizes[i] : cumulative_sizes[i + 1]
                        ].tolist()
                        for i in range(len(action_head_sizes))
                    ]
                else:
                    probs = None

            else:
                if (
                    return_log_prob
                    or return_entropy
                    or return_value
                    or return_probs
                    or extensions
                ):
                    raise ValueError(
                        "Plugins do not support returning additional information"
                    )
                # If it's a plugin, just go ahead and evaluate it on this thread - it's quick to process
                plugin = get_scripted_plugin(model_name)
                action = plugin.predict(observations, action_masks)
                log_probs = None
                entropy = None
                values = None
                probs = None
                ext_results = []

            response = Response(
                action=action.tolist()[0],
                logProb=log_probs.tolist()[0] if log_probs is not None else None,
                entropy=entropy.tolist()[0] if entropy is not None else None,
                values=values.tolist()[0] if values is not None else None,
                probs=probs,
                extensionResults=ext_results,
            )
            response_json = dataclasses.asdict(response)

            time_elapsed = time.time() - start_time
            logger.info(
                f"[{client_id}] Generated response in {time_elapsed:.4f} seconds: {response.action}"
            )

            response_line = (json.dumps(response_json) + "\n").encode()
            writer.write(response_line)
            await writer.drain()
            logger.debug(f"[{client_id}] Returned response: {response_line!r}")

    except OSError as e:
        logger.warning(f"[{client_id}] Caught exception in client handler: {e}")
    except Exception as e:
        logger.exception(f"[{client_id}] Caught exception in client handler: {e}")
    finally:
        writer.close()
        try:
            await writer.wait_closed()
        except Exception as e:
            logger.info(
                f"[{client_id}] Failed to wait for client connection to close: {e}"
            )
        logger.info(f"[{client_id}] Disconnected client")


async def preload_models(remote_processor: RemoteProcessor) -> None:
    # Load each model in each worker
    logger.info(
        f"Preloading {len(models)} models on {remote_processor.get_pool_size()} workers"
    )
    preload_model_tasks = [
        remote_processor.predict(process_id=i, model_path=model)
        for i in range(remote_processor.get_pool_size())
        for model in models.values()
    ]
    await asyncio.gather(*preload_model_tasks)
    logger.info(
        f"Preloaded {len(models)} models on {remote_processor.get_pool_size()} workers"
    )


async def run_api(
    host: str,
    port: int,
    remote_processor_pool_size: int,
    remote_processor_type: str,
    remote_processor_kwargs: dict[str, Any],
    device: str,
) -> None:
    logger.info(f"Starting agent server on {host}:{port}")
    async with await create_remote_processor(
        pool_size=remote_processor_pool_size,
        device=device,
        processor_type=remote_processor_type,
        remote_processor_additional_params=remote_processor_kwargs,
    ) as remote_processor:
        await preload_models(remote_processor)

        async def _handle_client_wrapper(
            reader: StreamReader, writer: StreamWriter
        ) -> None:
            await handle_client(reader, writer, remote_processor)

        server = await asyncio.start_server(_handle_client_wrapper, host, port)
        addr = server.sockets[0].getsockname()
        logger.info(f"Serving agents on {addr}: {list(models.keys())}")

        async with server:
            await server.serve_forever()


def main(argv: list[str]) -> None:
    parser = argparse.ArgumentParser(description="Agent serving script")
    parser.add_argument("--host", type=str, help="Host", default="127.0.0.1")
    parser.add_argument("--port", type=int, help="Port", default=9999)
    parser.add_argument(
        "--remote-processor-pool-size",
        type=int,
        help="Remote processor pool size",
        default=1,
    )
    parser.add_argument(
        "--remote-processor-type",
        type=str,
        help="Remote processor type",
        choices=REMOTE_PROCESSOR_TYPES,
        default=THREAD_REMOTE_PROCESSOR,
    )
    parser.add_argument(
        "--remote-processor-kwargs",
        type=json.loads,
        help="Remote processor kwargs",
        default={},
    )
    parser.add_argument(
        "--device",
        type=str,
        help="Processor device",
        default="cpu",
    )

    args = parser.parse_args(argv)

    remote_processor_kwargs = args.remote_processor_kwargs
    remote_processor_kwargs = replace_dash_with_underscore(remote_processor_kwargs)

    asyncio.run(
        run_api(
            host=args.host,
            port=args.port,
            remote_processor_pool_size=args.remote_processor_pool_size,
            remote_processor_type=args.remote_processor_type,
            remote_processor_kwargs=remote_processor_kwargs,
            device=args.device,
        )
    )


def main_entry_point() -> None:
    main(sys.argv[1:])


if __name__ == "__main__":
    main_entry_point()
