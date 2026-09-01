from pathlib import Path

from fastapi.testclient import TestClient

from waht_agent.agent.runner import AgentExecutionResult
from waht_agent.core.config import Settings
from waht_agent.main import create_app
from waht_agent.schemas.agent import (
    AgentMessageView,
    CreatedNote,
    DraftProposal,
    NoteDetail,
    NoteMetadata,
    NoteSearchItem,
    NoteSearchRequest,
)


class FakeCoreClient:
    def __init__(self) -> None:
        self.created_drafts: list[DraftProposal] = []

    async def search_notes(self, request: NoteSearchRequest, delegation_token: str) -> list[NoteSearchItem]:
        return []

    async def get_note(self, note_id: int, delegation_token: str) -> NoteDetail:
        raise AssertionError("本测试不应读取笔记")

    async def get_note_metadata(self, delegation_token: str) -> NoteMetadata:
        return NoteMetadata(categories=[], tags=[])

    async def create_note_draft(
        self,
        draft: DraftProposal,
        delegation_token: str,
        idempotency_key: str,
    ) -> CreatedNote:
        self.created_drafts.append(draft)
        return CreatedNote(id=101, title=draft.title, slug="agent-created-note", status="DRAFT")

    async def close(self) -> None:
        return None


class FakeAgentRunner:
    async def run(
        self,
        message: str,
        history: list[AgentMessageView],
        delegation_token: str,
    ) -> AgentExecutionResult:
        draft = DraftProposal(
            title="Java 并发学习提纲",
            summary="由 Agent 整理的待确认草稿",
            content="# Java 并发\n\n先理解 JMM，再学习锁。",
            category_id=1,
            tag_ids=[2],
        )
        return AgentExecutionResult(answer="我整理了一份草稿，请确认后保存。", pending_draft=draft)


def build_client(tmp_path: Path) -> tuple[TestClient, FakeCoreClient]:
    settings = Settings(
        database_url=f"sqlite+pysqlite:///{tmp_path / 'agent-test.db'}",
        service_token="test-service-token",
        ai_model="fake-model",
        openai_api_key="fake-key",
    )
    core_client = FakeCoreClient()
    app = create_app(settings=settings, runner=FakeAgentRunner(), core_client=core_client)
    return TestClient(app), core_client


def test_conversation_run_and_approval_flow(tmp_path: Path) -> None:
    client, core_client = build_client(tmp_path)
    headers = {"X-WAHT-Service-Token": "test-service-token"}
    actor = {"userId": 7, "username": "author"}

    with client:
        create_response = client.post(
            "/internal/v1/conversations",
            headers=headers,
            json={"actor": actor, "title": "并发学习"},
        )
        assert create_response.status_code == 200
        conversation_id = create_response.json()["data"]["id"]

        run_response = client.post(
            f"/internal/v1/conversations/{conversation_id}/messages",
            headers=headers,
            json={
                "actor": actor,
                "runId": "run-001",
                "message": "帮我整理为学习笔记",
                "delegationToken": "delegation-token",
            },
        )
        assert run_response.status_code == 200
        turn = run_response.json()["data"]
        assert turn["status"] == "WAITING_APPROVAL"
        approval_id = turn["approval"]["id"]

        approval_response = client.post(
            f"/internal/v1/approvals/{approval_id}",
            headers=headers,
            json={
                "actor": actor,
                "decision": "APPROVE",
                "delegationToken": "delegation-token-2",
            },
        )
        assert approval_response.status_code == 200
        assert approval_response.json()["data"]["status"] == "COMPLETED"
        assert approval_response.json()["data"]["approval"]["createdNoteId"] == 101
        assert len(core_client.created_drafts) == 1

        detail_response = client.get(
            f"/internal/v1/conversations/{conversation_id}",
            headers=headers,
            params={"userId": 7, "username": "author"},
        )
        assert detail_response.status_code == 200
        roles = [item["role"] for item in detail_response.json()["data"]["messages"]]
        assert roles == ["USER", "ASSISTANT", "ASSISTANT"]
        assert detail_response.json()["data"]["pendingApproval"] is None


def test_service_token_and_user_isolation(tmp_path: Path) -> None:
    client, _ = build_client(tmp_path)
    headers = {"X-WAHT-Service-Token": "test-service-token"}

    with client:
        unauthorized = client.get(
            "/internal/v1/conversations",
            params={"userId": 7, "username": "author"},
        )
        assert unauthorized.status_code == 401
        assert unauthorized.json()["code"] == 401

        created = client.post(
            "/internal/v1/conversations",
            headers=headers,
            json={"actor": {"userId": 7, "username": "author"}},
        ).json()["data"]
        hidden = client.get(
            f"/internal/v1/conversations/{created['id']}",
            headers=headers,
            params={"userId": 8, "username": "other"},
        )
        assert hidden.status_code == 404
        assert hidden.json()["message"] == "对话不存在"


def test_message_returns_503_when_model_is_not_configured(tmp_path: Path) -> None:
    settings = Settings(
        database_url=f"sqlite+pysqlite:///{tmp_path / 'agent-unconfigured.db'}",
        service_token="test-service-token",
        ai_model="",
        openai_api_key="",
    )
    app = create_app(settings=settings, core_client=FakeCoreClient())
    headers = {"X-WAHT-Service-Token": "test-service-token"}
    actor = {"userId": 7, "username": "author"}

    with TestClient(app) as client:
        conversation = client.post(
            "/internal/v1/conversations",
            headers=headers,
            json={"actor": actor},
        ).json()["data"]
        response = client.post(
            f"/internal/v1/conversations/{conversation['id']}/messages",
            headers=headers,
            json={
                "actor": actor,
                "runId": "run-unconfigured",
                "message": "解释一下 JMM",
                "delegationToken": "delegation-token",
            },
        )

        assert response.status_code == 503
        assert response.json()["code"] == 503
        assert "OPENAI_API_KEY" in response.json()["message"]
