from datetime import datetime
from enum import StrEnum

from pydantic import Field

from waht_agent.core.responses import ApiModel


class RunStatus(StrEnum):
    CREATED = "CREATED"
    RUNNING = "RUNNING"
    WAITING_APPROVAL = "WAITING_APPROVAL"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"
    CANCELLED = "CANCELLED"


class ApprovalDecision(StrEnum):
    APPROVE = "APPROVE"
    EDIT = "EDIT"
    REJECT = "REJECT"


class Actor(ApiModel):
    user_id: int = Field(gt=0)
    username: str = Field(min_length=1, max_length=50)


class ConversationCreateRequest(ApiModel):
    actor: Actor
    title: str | None = Field(default=None, max_length=120)


class AgentRunRequest(ApiModel):
    actor: Actor
    run_id: str = Field(min_length=1, max_length=64)
    message: str = Field(min_length=1, max_length=8000)
    delegation_token: str = Field(min_length=1)


class NoteSource(ApiModel):
    note_id: int
    title: str
    slug: str
    status: str


class DraftProposal(ApiModel):
    title: str = Field(min_length=1, max_length=120)
    summary: str = Field(default="", max_length=500)
    content: str = Field(default="", max_length=200000)
    category_id: int = Field(gt=0)
    tag_ids: list[int] = Field(default_factory=list, max_length=10)


class ApprovalView(ApiModel):
    id: str
    status: str
    action: str
    draft: DraftProposal
    created_note_id: int | None = None


class AgentMessageView(ApiModel):
    id: str
    role: str
    content: str
    sources: list[NoteSource]
    created_at: datetime


class ConversationSummary(ApiModel):
    id: str
    title: str
    status: str
    created_at: datetime
    updated_at: datetime


class ConversationDetail(ConversationSummary):
    messages: list[AgentMessageView]
    pending_approval: ApprovalView | None = None


class AgentTurnResponse(ApiModel):
    conversation_id: str
    run_id: str
    status: RunStatus
    answer: str
    sources: list[NoteSource]
    approval: ApprovalView | None = None


class ApprovalRequest(ApiModel):
    actor: Actor
    decision: ApprovalDecision
    delegation_token: str = Field(min_length=1)
    draft: DraftProposal | None = None


class NoteSearchRequest(ApiModel):
    keyword: str = Field(min_length=1, max_length=100)
    category_id: int | None = Field(default=None, gt=0)
    tag_ids: list[int] = Field(default_factory=list, max_length=10)
    limit: int = Field(default=5, ge=1, le=10)


class NoteSearchItem(ApiModel):
    id: int
    title: str
    slug: str
    summary: str | None = None
    status: str
    category_name: str | None = None
    tag_names: list[str] = Field(default_factory=list)
    updated_at: datetime | None = None


class NoteDetail(ApiModel):
    id: int
    title: str
    slug: str
    summary: str | None = None
    content: str
    status: str
    category_id: int
    category_name: str | None = None
    tag_ids: list[int] = Field(default_factory=list)
    tag_names: list[str] = Field(default_factory=list)
    updated_at: datetime | None = None


class NoteCategory(ApiModel):
    id: int
    name: str
    slug: str


class NoteTag(ApiModel):
    id: int
    name: str
    slug: str


class NoteMetadata(ApiModel):
    categories: list[NoteCategory]
    tags: list[NoteTag]


class CreatedNote(ApiModel):
    id: int
    title: str
    slug: str
    status: str
