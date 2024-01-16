import gzip
import io
import os
import pickle
import zipfile
from typing import Any


def create_zip(src_dir: str, compression: int = zipfile.ZIP_DEFLATED) -> bytes:
    buffer = io.BytesIO()
    with zipfile.ZipFile(buffer, "w", compression) as zip_file:
        for root, dirs, files in os.walk(src_dir):
            for file in files:
                zip_file.write(
                    os.path.join(root, file),
                    os.path.relpath(os.path.join(root, file), src_dir),
                )
    return buffer.getvalue()


def unzip(zip_bytes: bytes, out_dir: str) -> None:
    with zipfile.ZipFile(io.BytesIO(zip_bytes), "r") as zip_file:
        zip_file.extractall(out_dir)


def decompress_and_unpickle(compressed_pickle: bytes) -> Any:
    compressed_pickle_io = io.BytesIO(compressed_pickle)
    with gzip.GzipFile(fileobj=compressed_pickle_io, mode="rb") as gz_file:
        pickle_bytes = gz_file.read()
    obj = pickle.loads(pickle_bytes)
    return obj


def pickle_and_compress(obj: Any) -> bytes:
    pickle_bytes = pickle.dumps(obj)
    compressed_pickle_io = io.BytesIO()
    with gzip.GzipFile(fileobj=compressed_pickle_io, mode="wb") as gz_file:
        gz_file.write(pickle_bytes)
    return compressed_pickle_io.getvalue()


def save_compressed_pickle_to_file(obj: Any, file_path: str) -> None:
    with open(file_path, "wb") as f:
        f.write(pickle_and_compress(obj))


def load_compressed_pickle_from_file(file_path: str) -> Any:
    with open(file_path, "rb") as f:
        compressed_pickle = f.read()
    return decompress_and_unpickle(compressed_pickle)
