from contextlib import AbstractContextManager
from typing import Any
from uuid import uuid4

from sqlalchemy import Select, select
from sqlalchemy.orm import Session

from waht_agent.core.errors import ConflictError, NotFoundError
from waht_agent.persistence.database import Database
from waht_agent.persistence.models import (
    ApprovalModel,
    ConversationModel,
    MessageModel,
    RunModel,
    ToolCallModel,
    utc_now,
)


class AgentRepository:
    """集中维护 Agent 会话状态，业务服务不直接拼装 SQLAlchemy 查询。"""

    def __init__(self, database: Database) -> None:
        self.database = database

    def create_conversation(self, user_id: int, title: str | None) -> ConversationModel:
        conversation = ConversationModel(
            id=str(uuid4()),
            user_id=user_id,
            title=(title or "新的学习对话").strip() or "新的学习对话",
            status="ACTIVE",
        )
        with self._session() as session:
            session.add(conversation)
        return conversation

    def list_conversations(self, user_id: int) -> list[ConversationModel]:
        with self._session() as session:
            statement = (
                select(ConversationModel)
                .where(ConversationModel.user_id == user_id)
                .order_by(ConversationModel.updated_at.desc())
            )
            return list(session.scalars(statement))

    def get_conversation(self, conversation_id: str, user_id: int) -> ConversationModel:
        with self._session() as session:
            return self._get_owned_conversation(session, conversation_id, user_id)

    def list_messages(self, conversation_id: str, user_id: int) -> list[MessageModel]:
        with self._session() as session:
            self._get_owned_conversation(session, conversation_id, user_id)
            statement = (
                select(MessageModel)
                .where(MessageModel.conversation_id == conversation_id)
                .order_by(MessageModel.created_at.asc(), MessageModel.id.asc())
            )
            return list(session.scalars(statement))

    def add_message(
        self,
        conversation_id: str,
        user_id: int,
        role: str,
        content: str,
        sources: list[dict[str, Any]] | None = None,
    ) -> MessageModel:
        message = MessageModel(
            id=str(uuid4()),
            conversation_id=conversation_id,
            role=role,
            content=content,
            sources=sources or [],
        )
        with self._session() as session:
            conversation = self._get_owned_conversation(session, conversation_id, user_id)
            conversation.updated_at = utc_now()
            session.add(message)
        return message

    def create_run(self, run_id: str, conversation_id: str, user_id: int, model: str) -> RunModel:
        run = RunModel(
            id=run_id,
            conversation_id=conversation_id,
            user_id=user_id,
            status="RUNNING",
            model=model or "unconfigured",
        )
        with self._session() as session:
            self._get_owned_conversation(session, conversation_id, user_id)
            if session.get(RunModel, run_id):
                raise ConflictError("Agent 运行 ID 已存在")
            session.add(run)
        return run

    def finish_run(
        self,
        run_id: str,
        status: str,
        input_tokens: int = 0,
        output_tokens: int = 0,
        total_tokens: int = 0,
        error_message: str | None = None,
    ) -> RunModel:
        with self._session() as session:
            run = self._get_run(session, run_id)
            run.status = status
            run.input_tokens = input_tokens
            run.output_tokens = output_tokens
            run.total_tokens = total_tokens
            run.error_message = error_message
            run.finished_at = utc_now() if status in {"COMPLETED", "FAILED", "CANCELLED"} else None
            return run

    def add_tool_call(
        self,
        run_id: str,
        tool_name: str,
        arguments: dict[str, Any],
        result_summary: dict[str, Any],
        status: str = "COMPLETED",
    ) -> ToolCallModel:
        tool_call = ToolCallModel(
            id=str(uuid4()),
            run_id=run_id,
            tool_name=tool_name,
            arguments=arguments,
            result_summary=result_summary,
            status=status,
        )
        with self._session() as session:
            self._get_run(session, run_id)
            session.add(tool_call)
        return tool_call

    def create_approval(self, run_id: str, action: str, payload: dict[str, Any]) -> ApprovalModel:
        approval = ApprovalModel(
            id=str(uuid4()),
            run_id=run_id,
            action=action,
            status="PENDING",
            payload=payload,
        )
        with self._session() as session:
            self._get_run(session, run_id)
            session.add(approval)
        return approval

    def get_pending_approval(self, conversation_id: str, user_id: int) -> ApprovalModel | None:
        with self._session() as session:
            self._get_owned_conversation(session, conversation_id, user_id)
            statement = (
                select(ApprovalModel)
                .join(RunModel, RunModel.id == ApprovalModel.run_id)
                .where(
                    RunModel.conversation_id == conversation_id,
                    RunModel.user_id == user_id,
                    ApprovalModel.status == "PENDING",
                )
                .order_by(ApprovalModel.created_at.desc())
            )
            return session.scalars(statement).first()

    def get_approval(self, approval_id: str, user_id: int) -> ApprovalModel:
        with self._session() as session:
            return self._get_owned_approval(session, approval_id, user_id)

    def decide_approval(
        self,
        approval_id: str,
        user_id: int,
        status: str,
        decided_payload: dict[str, Any] | None,
        created_note_id: int | None,
    ) -> ApprovalModel:
        with self._session() as session:
            approval = self._get_owned_approval(session, approval_id, user_id)
            if approval.status != "PENDING":
                return approval
            approval.status = status
            approval.decided_payload = decided_payload
            approval.created_note_id = created_note_id
            approval.decided_at = utc_now()
            return approval

    def get_run_for_approval(self, approval_id: str, user_id: int) -> RunModel:
        with self._session() as session:
            approval = self._get_owned_approval(session, approval_id, user_id)
            run = self._get_run(session, approval.run_id)
            if run.user_id != user_id:
                raise NotFoundError("审批不存在")
            return run

    def _session(self) -> AbstractContextManager[Session]:
        return self.database.session()

    @staticmethod
    def _get_owned_conversation(session: Session, conversation_id: str, user_id: int) -> ConversationModel:
        conversation = session.get(ConversationModel, conversation_id)
        if conversation is None or conversation.user_id != user_id:
            raise NotFoundError("对话不存在")
        return conversation

    @staticmethod
    def _get_run(session: Session, run_id: str) -> RunModel:
        run = session.get(RunModel, run_id)
        if run is None:
            raise NotFoundError("Agent 运行不存在")
        return run

    @staticmethod
    def _get_owned_approval(session: Session, approval_id: str, user_id: int) -> ApprovalModel:
        statement: Select[tuple[ApprovalModel]] = (
            select(ApprovalModel)
            .join(RunModel, RunModel.id == ApprovalModel.run_id)
            .where(ApprovalModel.id == approval_id, RunModel.user_id == user_id)
        )
        approval = session.scalars(statement).first()
        if approval is None:
            raise NotFoundError("审批不存在")
        return approval
