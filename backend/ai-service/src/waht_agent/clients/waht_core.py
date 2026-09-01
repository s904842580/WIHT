from collections.abc import Mapping
from typing import Protocol, TypeVar

import httpx
from pydantic import BaseModel

from waht_agent.core.errors import AppError, AgentUnavailableError
from waht_agent.schemas.agent import (
    CreatedNote,
    DraftProposal,
    NoteDetail,
    NoteMetadata,
    NoteSearchItem,
    NoteSearchRequest,
)

ModelT = TypeVar("ModelT", bound=BaseModel)


class WahtCoreClient(Protocol):
    """Python Agent 访问 Java 业务能力的窄接口。"""

    async def search_notes(self, request: NoteSearchRequest, delegation_token: str) -> list[NoteSearchItem]: ...

    async def get_note(self, note_id: int, delegation_token: str) -> NoteDetail: ...

    async def get_note_metadata(self, delegation_token: str) -> NoteMetadata: ...

    async def create_note_draft(
        self,
        draft: DraftProposal,
        delegation_token: str,
        idempotency_key: str,
    ) -> CreatedNote: ...

    async def close(self) -> None: ...


class HttpWahtCoreClient:
    """通过内部 HTTP API 调用 Java，永远不直接读取 waht 业务库。"""

    def __init__(self, base_url: str, service_token: str, timeout_seconds: float) -> None:
        self._client = httpx.AsyncClient(base_url=base_url.rstrip("/"), timeout=timeout_seconds)
        self._service_token = service_token

    async def search_notes(self, request: NoteSearchRequest, delegation_token: str) -> list[NoteSearchItem]:
        data = await self._request(
            "POST",
            "/api/internal/agent-tools/notes/search",
            delegation_token,
            json=request.model_dump(by_alias=True),
        )
        if not isinstance(data, list):
            raise AgentUnavailableError("Java 笔记搜索接口返回格式错误")
        return [NoteSearchItem.model_validate(item) for item in data]

    async def get_note(self, note_id: int, delegation_token: str) -> NoteDetail:
        data = await self._request("GET", f"/api/internal/agent-tools/notes/{note_id}", delegation_token)
        return NoteDetail.model_validate(data)

    async def get_note_metadata(self, delegation_token: str) -> NoteMetadata:
        data = await self._request("GET", "/api/internal/agent-tools/note-metadata", delegation_token)
        return NoteMetadata.model_validate(data)

    async def create_note_draft(
        self,
        draft: DraftProposal,
        delegation_token: str,
        idempotency_key: str,
    ) -> CreatedNote:
        data = await self._request(
            "POST",
            "/api/internal/agent-tools/note-drafts",
            delegation_token,
            json=draft.model_dump(by_alias=True),
            extra_headers={"Idempotency-Key": idempotency_key},
        )
        return CreatedNote.model_validate(data)

    async def close(self) -> None:
        await self._client.aclose()

    async def _request(
        self,
        method: str,
        path: str,
        delegation_token: str,
        json: dict[str, object] | None = None,
        extra_headers: Mapping[str, str] | None = None,
    ) -> object:
        headers = {
            "Authorization": f"Bearer {delegation_token}",
            "X-WAHT-Service-Token": self._service_token,
        }
        if extra_headers:
            headers.update(extra_headers)
        try:
            response = await self._client.request(method, path, json=json, headers=headers)
        except httpx.HTTPError as exc:
            raise AgentUnavailableError("无法连接 Java 核心服务") from exc

        try:
            body = response.json()
        except ValueError as exc:
            raise AgentUnavailableError("Java 核心服务返回了无效响应") from exc
        if not isinstance(body, dict):
            raise AgentUnavailableError("Java 核心服务返回格式错误")

        code = body.get("code")
        message = body.get("message")
        if response.is_error or code not in {0, 200}:
            error_code = int(code) if isinstance(code, int) else response.status_code
            error_message = str(message) if message else "Java 核心服务调用失败"
            raise AppError(error_code, error_message, response.status_code)
        return body.get("data")
