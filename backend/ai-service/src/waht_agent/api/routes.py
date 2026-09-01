import secrets

from fastapi import APIRouter, Header, Query, Request

from waht_agent.core.errors import AppError
from waht_agent.core.responses import BaseResponse, success
from waht_agent.schemas.agent import (
    Actor,
    AgentRunRequest,
    AgentTurnResponse,
    ApprovalRequest,
    ConversationCreateRequest,
    ConversationDetail,
    ConversationSummary,
)
from waht_agent.service import AgentService

router = APIRouter(prefix="/internal/v1")


def get_service(request: Request) -> AgentService:
    return request.app.state.agent_service


def verify_service_token(request: Request, token: str | None) -> None:
    expected = request.app.state.settings.service_token
    if token is None or not secrets.compare_digest(token, expected):
        raise AppError(401, "服务认证失败", 401)


@router.get("/conversations", response_model=BaseResponse[list[ConversationSummary]])
def list_conversations(
    request: Request,
    user_id: int = Query(alias="userId", gt=0),
    username: str = Query(min_length=1, max_length=50),
    service_token: str | None = Header(default=None, alias="X-WAHT-Service-Token"),
) -> BaseResponse[list[ConversationSummary]]:
    verify_service_token(request, service_token)
    return success(get_service(request).list_conversations(Actor(user_id=user_id, username=username)))


@router.post("/conversations", response_model=BaseResponse[ConversationSummary])
def create_conversation(
    body: ConversationCreateRequest,
    request: Request,
    service_token: str | None = Header(default=None, alias="X-WAHT-Service-Token"),
) -> BaseResponse[ConversationSummary]:
    verify_service_token(request, service_token)
    return success(get_service(request).create_conversation(body.actor, body.title))


@router.get("/conversations/{conversation_id}", response_model=BaseResponse[ConversationDetail])
def get_conversation(
    conversation_id: str,
    request: Request,
    user_id: int = Query(alias="userId", gt=0),
    username: str = Query(min_length=1, max_length=50),
    service_token: str | None = Header(default=None, alias="X-WAHT-Service-Token"),
) -> BaseResponse[ConversationDetail]:
    verify_service_token(request, service_token)
    actor = Actor(user_id=user_id, username=username)
    return success(get_service(request).get_conversation(conversation_id, actor))


@router.post("/conversations/{conversation_id}/messages", response_model=BaseResponse[AgentTurnResponse])
async def run_agent(
    conversation_id: str,
    body: AgentRunRequest,
    request: Request,
    service_token: str | None = Header(default=None, alias="X-WAHT-Service-Token"),
) -> BaseResponse[AgentTurnResponse]:
    verify_service_token(request, service_token)
    return success(await get_service(request).run(conversation_id, body))


@router.post("/approvals/{approval_id}", response_model=BaseResponse[AgentTurnResponse])
async def decide_approval(
    approval_id: str,
    body: ApprovalRequest,
    request: Request,
    service_token: str | None = Header(default=None, alias="X-WAHT-Service-Token"),
) -> BaseResponse[AgentTurnResponse]:
    verify_service_token(request, service_token)
    return success(await get_service(request).decide_approval(approval_id, body))
