import asyncio
import dataclasses
import json
from asyncio import StreamReader, StreamWriter
from typing import Any

from pvp_ml.api import Request, Response


class ApiClient:
    def __init__(self, host: str = "localhost", port: int = 9999):
        self._host = host
        self._port = port
        self._reader: StreamReader | None = None
        self._writer: StreamWriter | None = None

    async def send_request(self, request: Request) -> Response:
        if self._writer is None:
            await self._connect()

        assert self._writer is not None
        assert self._reader is not None

        try:
            request_json = json.dumps(dataclasses.asdict(request))
            self._writer.write((request_json + "\n").encode())
            await self._writer.drain()

            response_json = await self._reader.readline()
            if not response_json:
                raise IOError

            response_dict = json.loads(response_json.decode())
            return Response(**response_dict)

        except Exception as e:
            await self.close()
            raise e

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

    async def __aenter__(self) -> "ApiClient":
        if self._writer is None:
            await self._connect()
        return self

    async def __aexit__(self, *args: Any) -> None:
        await self.close()
