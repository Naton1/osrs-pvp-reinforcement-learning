import asyncio
import logging
import multiprocessing as mp
import pickle
import socket
import struct
from asyncio import Server, StreamReader, StreamWriter
from functools import lru_cache
from typing import Any

import torch as th

from pvp_ml.ppo.ppo import PPO
from pvp_ml.util.remote_processor.remote_processor import RemoteProcessor

logger = logging.getLogger(__name__)
SIZE_BUFFER_LENGTH = 4


def _recv_exactly(sock: socket.socket, size: int) -> bytes:
    data = bytearray()
    while len(data) < size:
        chunk = sock.recv(size - len(data))
        if not chunk:
            raise ValueError(
                f"Read 0 bytes from server, likely disconnected (read {len(data)} so far, expecting total {size})"
            )
        data.extend(chunk)
    return bytes(data)


def _worker(
    connection_id: int,
    host: str,
    port: int,
    device: str,
    cache_size: int = 1000,
) -> None:
    try:
        logger.info(f"Running process worker {connection_id}")
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
            sock.connect((host, port))
            sock.sendall(struct.pack("!I", connection_id))

            @lru_cache(maxsize=cache_size)
            def load_model(_model_path: str) -> PPO:
                logger.info(
                    f"Worker {connection_id} loading model {_model_path} into memory"
                )
                return PPO.load(_model_path, device=device, trainable=False)

            while True:
                size_bytes = _recv_exactly(sock, SIZE_BUFFER_LENGTH)
                if not size_bytes:
                    logger.info(
                        f"Received end, terminating worker process {connection_id}"
                    )
                    break
                size = struct.unpack("!I", size_bytes)[0]
                logger.debug(f"Reading {size} bytes from server")
                request_bytes = _recv_exactly(sock, size)
                assert size == len(
                    request_bytes
                ), f"Read {len(request_bytes)} from server but expected {size}"
                (
                    model_path,
                    obs,
                    action_masks,
                    deterministic,
                    return_actions,
                    return_log_probs,
                    return_entropy,
                    return_values,
                    return_probs,
                    extensions,
                ) = pickle.loads(request_bytes)
                model = load_model(model_path)
                if obs is None:
                    # preload model, don't actually predict
                    prediction = None
                    log_probs = None
                    entropy = None
                    values = None
                    probs = None
                    ext_results: list[Any] = []
                else:
                    (
                        prediction,
                        log_probs,
                        entropy,
                        values,
                        probs,
                        ext_results,
                    ) = model.predict(
                        obs.to(device),
                        action_masks.to(device),
                        deterministic=deterministic,
                        return_actions=return_actions,
                        return_values=return_values,
                        return_log_probs=return_log_probs,
                        return_entropy=return_entropy,
                        return_probs=return_probs,
                        extensions=extensions,
                        return_device="cpu",
                    )
                response_bytes = pickle.dumps(
                    (prediction, log_probs, entropy, values, probs, ext_results)
                )
                length_bytes = struct.pack("!I", len(response_bytes))
                sock.sendall(length_bytes)
                sock.sendall(response_bytes)
    except (BrokenPipeError, ConnectionResetError):
        logger.info(f"Remote socket closed: {connection_id}")
    except KeyboardInterrupt:
        logger.info(f"Process interrupted: {connection_id}")
    except Exception:
        logger.exception(f"Caught error in worker: {connection_id}")
    finally:
        logger.info(f"Worker process completed: {connection_id}")


class Worker:
    def __init__(self, process: mp.process.BaseProcess):
        self.process = process
        self.lock = asyncio.Lock()
        self.initialized = asyncio.Event()
        self.reader: StreamReader | None = None
        self.writer: StreamWriter | None = None

    def initialize(self, reader: StreamReader, writer: StreamWriter) -> None:
        assert not self.initialized.is_set()
        self.reader = reader
        self.writer = writer
        self.initialized.set()


class ExternalProcessor(RemoteProcessor):
    def __init__(self, pool_size: int, host: str = "127.0.0.1", device: str = "cpu"):
        self._host = host
        self._pool_size = pool_size
        self._workers: list[Worker] = []
        self._server: Server | None = None
        self._device = device

    async def _run_server(self, reader: StreamReader, writer: StreamWriter) -> None:
        logger.debug("Remote processor received connection")
        connection_id_bytes = await reader.readexactly(SIZE_BUFFER_LENGTH)
        connection_id = struct.unpack("!I", connection_id_bytes)[0]
        self._workers[connection_id].initialize(reader, writer)
        logger.info(f"Remote processor {connection_id} initialized")

    def get_pool_size(self) -> int:
        return self._pool_size

    def get_device(self) -> str:
        return self._device

    async def initialize(self) -> None:
        assert self._server is None
        logger.info(f"Initializing remote processor on {self._host}")
        ctx = mp.get_context("spawn")  # Can't fork with GPU support
        self._server = await asyncio.start_server(self._run_server, self._host)
        await self._server.start_serving()
        server_port = self._server.sockets[0].getsockname()[1]
        logger.info(f"Initialized remote processor on {self._host}:{server_port}")
        self._workers = [
            Worker(
                ctx.Process(
                    target=_worker,
                    args=(connection_id, self._host, server_port, self._device),
                    daemon=True,
                    name=f"Remote Processor {connection_id}",
                )
            )
            for connection_id in range(self._pool_size)
        ]
        for worker in self._workers:
            worker.process.start()
        logger.info(
            f"Started {self._pool_size} workers, waiting to receive connections..."
        )
        await asyncio.gather(*[worker.initialized.wait() for worker in self._workers])
        logger.info(f"Remote processor initialized on {self._host}:{server_port}")

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
        process_worker = self._workers[process_id]
        # Serialize on CPU, remote processor will convert to GPU if needed
        if return_device is None and observation is not None:
            return_device = str(observation.device)
        if observation is not None:
            observation = observation.cpu()
        if action_masks is not None:
            action_masks = action_masks.cpu()
        async with process_worker.lock:
            assert process_worker.reader is not None
            assert process_worker.writer is not None
            body = pickle.dumps(
                (
                    model_path,
                    observation,
                    action_masks,
                    deterministic,
                    return_actions,
                    return_log_probs,
                    return_entropy,
                    return_values,
                    return_probs,
                    extensions,
                )
            )
            length_bytes = struct.pack("!I", len(body))
            process_worker.writer.write(length_bytes)
            process_worker.writer.write(body)
            await process_worker.writer.drain()
            size_bytes = await process_worker.reader.readexactly(SIZE_BUFFER_LENGTH)
            size = struct.unpack("!I", size_bytes)[0]
            response_bytes = await process_worker.reader.readexactly(size)
            actions, log_probs, entropy, values, probs, ext_results = pickle.loads(
                response_bytes
            )
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

    async def close(self) -> None:
        if self._server is None:
            # Not initialized
            return
        logger.info("Closing remote processor")
        for worker in self._workers:
            async with worker.lock:
                if worker.writer is not None:
                    worker.writer.close()
                    logger.debug(f"Waiting for writer to close: {worker}")
                    await worker.writer.wait_closed()
        logger.debug("Waiting for server to close")
        self._server.close()
        await self._server.wait_closed()
        for worker in self._workers:
            logger.debug(f"Joining processes: {worker}")
            # These will block the event loop
            # hopefully won't take long, and this is only called at the end anyway
            worker.process.join()
        logger.info("Remote processor closed")
