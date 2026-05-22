#!/usr/bin/env python3
"""
Reset the global H5 visit counter stored in Redis.

The H5 login/start pages read and increment Redis key `user:viewNum` through
`/wx/user/getViewNum` and `/wx/user/addViewNum`. Keep the key present and set it
to a numeric string; deleting it would let the Java service initialize it back
to 71940 on the next increment.
"""

import argparse
import os
import socket
import sys
from typing import Iterable, Optional


DEFAULT_HOST = "127.0.0.1"
DEFAULT_PORT = 6379
DEFAULT_DB = 1
DEFAULT_PASSWORD = "citywalkRedis"
DEFAULT_KEY = "user:viewNum"
DEFAULT_VALUE = "0"


class RedisError(RuntimeError):
    pass


class RedisClient:
    def __init__(self, host: str, port: int, timeout: float) -> None:
        self._sock = socket.create_connection((host, port), timeout=timeout)
        self._file = self._sock.makefile("rb")

    def close(self) -> None:
        try:
            self._file.close()
        finally:
            self._sock.close()

    def command(self, *parts: object) -> object:
        payload = self._encode_command(parts)
        self._sock.sendall(payload)
        return self._read_response()

    @staticmethod
    def _encode_command(parts: Iterable[object]) -> bytes:
        encoded_parts = [str(part).encode("utf-8") for part in parts]
        payload = [f"*{len(encoded_parts)}\r\n".encode("ascii")]
        for part in encoded_parts:
            payload.append(f"${len(part)}\r\n".encode("ascii"))
            payload.append(part)
            payload.append(b"\r\n")
        return b"".join(payload)

    def _read_line(self) -> bytes:
        line = self._file.readline()
        if not line:
            raise RedisError("Redis connection closed unexpectedly")
        if not line.endswith(b"\r\n"):
            raise RedisError(f"Invalid Redis response line: {line!r}")
        return line[:-2]

    def _read_response(self) -> object:
        prefix = self._file.read(1)
        if not prefix:
            raise RedisError("Redis connection closed unexpectedly")

        if prefix == b"+":
            return self._read_line().decode("utf-8")
        if prefix == b"-":
            raise RedisError(self._read_line().decode("utf-8"))
        if prefix == b":":
            return int(self._read_line())
        if prefix == b"$":
            length = int(self._read_line())
            if length == -1:
                return None
            data = self._file.read(length)
            crlf = self._file.read(2)
            if crlf != b"\r\n":
                raise RedisError("Invalid Redis bulk string terminator")
            return data.decode("utf-8")
        if prefix == b"*":
            count = int(self._read_line())
            if count == -1:
                return None
            return [self._read_response() for _ in range(count)]

        raise RedisError(f"Unsupported Redis response prefix: {prefix!r}")


def env_or_default(name: str, default: object) -> object:
    value = os.getenv(name)
    return default if value is None or value == "" else value


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Reset Redis key user:viewNum to 0 without deleting it.",
    )
    parser.add_argument("--host", default=env_or_default("REDIS_HOST", DEFAULT_HOST))
    parser.add_argument(
        "--port",
        type=int,
        default=int(env_or_default("REDIS_PORT", DEFAULT_PORT)),
    )
    parser.add_argument(
        "--db",
        type=int,
        default=int(env_or_default("REDIS_DB", DEFAULT_DB)),
    )
    parser.add_argument(
        "--password",
        default=env_or_default("REDIS_PASSWORD", DEFAULT_PASSWORD),
        help="Redis password. Use an empty string to skip AUTH.",
    )
    parser.add_argument("--key", default=env_or_default("REDIS_KEY", DEFAULT_KEY))
    parser.add_argument("--value", default=env_or_default("REDIS_VALUE", DEFAULT_VALUE))
    parser.add_argument(
        "--timeout",
        type=float,
        default=float(env_or_default("REDIS_TIMEOUT", 5)),
    )
    return parser.parse_args()


def printable(value: Optional[object]) -> str:
    return "<nil>" if value is None else str(value)


def main() -> int:
    args = parse_args()
    client: Optional[RedisClient] = None

    try:
        client = RedisClient(args.host, args.port, args.timeout)
        if args.password:
            client.command("AUTH", args.password)
        client.command("SELECT", args.db)

        before = client.command("GET", args.key)
        print(f"Redis: {args.host}:{args.port}, db={args.db}")
        print(f"Before: {args.key} = {printable(before)}")

        client.command("SET", args.key, args.value)
        after = client.command("GET", args.key)
        print(f"After:  {args.key} = {printable(after)}")
        return 0
    except (OSError, RedisError) as exc:
        print(f"Failed to reset {args.key}: {exc}", file=sys.stderr)
        return 1
    finally:
        if client is not None:
            client.close()


if __name__ == "__main__":
    raise SystemExit(main())
