import asyncio
import socket


def is_port_taken(port: int, address: str = "localhost") -> bool:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        try:
            s.settimeout(5)
            s.connect((address, port))
            return True
        except socket.error:
            pass
    return False


async def is_port_taken_async(port: int, address: str = "localhost") -> bool:
    try:
        _, writer = await asyncio.wait_for(
            asyncio.open_connection(address, port), timeout=5
        )
        writer.close()
        await writer.wait_closed()
        return True
    except (asyncio.TimeoutError, OSError):
        return False
