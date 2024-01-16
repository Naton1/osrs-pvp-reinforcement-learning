import asyncio
import json
import logging
from asyncio import StreamReader, StreamWriter
from typing import Any

logger = logging.getLogger(__name__)


class RemoteEnvConnector:
    def __init__(self, env_id: str, host: str = "localhost", port: int = 7070):
        self._env_id = env_id
        self._host = host
        self._port = port
        self._reader: StreamReader | None = None
        self._writer: StreamWriter | None = None

    async def send(self, action: str, body: dict[str, Any] | None = None) -> Any:
        if self._writer is None:
            await self._connect()
        assert self._reader is not None
        assert self._writer is not None
        request = {
            "action": action,
            "body": body,
            "meta": {"id": self._env_id},
        }
        request_str = json.dumps(request)
        logger.debug(f"Sending {action} request to {self._env_id}")
        self._writer.write((request_str + "\n").encode())
        await self._writer.drain()
        response = await self._reader.readline()
        logger.debug(f"Received {action} response from {self._env_id}")
        if not response:
            raise ValueError(
                f"Received no response from remote env {self._env_id}, connection likely closed: {response!r}"
            )
        result = json.loads(response)
        if result.get("error", False):
            raise ValueError(f"Remote request error for {self._env_id}: {response!r}")
        return result.get("body")

    async def close(self) -> None:
        if self._writer is not None:
            self._writer.close()
            try:
                await self._writer.wait_closed()
            except ConnectionError:
                pass
            self._reader = None
            self._writer = None

    async def _connect(self) -> None:
        self._reader, self._writer = await asyncio.open_connection(
            self._host, self._port
        )

    async def __aenter__(self) -> "RemoteEnvConnector":
        if self._writer is None:
            await self._connect()
        return self

    async def __aexit__(self, *args: Any) -> None:
        await self.close()
