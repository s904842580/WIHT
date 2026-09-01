from datetime import datetime, timezone
from typing import Generic, TypeVar

from pydantic import BaseModel, ConfigDict

T = TypeVar("T")


def to_camel(value: str) -> str:
    parts = value.split("_")
    return parts[0] + "".join(part.capitalize() for part in parts[1:])


class ApiModel(BaseModel):
    """统一使用 camelCase JSON，和 Java、TypeScript 字段保持一致。"""

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True, from_attributes=True)


class BaseResponse(ApiModel, Generic[T]):
    code: int
    message: str
    data: T | None
    timestamp: datetime


def success(data: T | None = None) -> BaseResponse[T]:
    return BaseResponse(code=0, message="success", data=data, timestamp=datetime.now(timezone.utc))


def failure(code: int, message: str) -> BaseResponse[None]:
    return BaseResponse(code=code, message=message, data=None, timestamp=datetime.now(timezone.utc))
