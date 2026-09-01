from waht_agent.agent.runner import AgentRunner
from waht_agent.clients.waht_core import WahtCoreClient
from waht_agent.core.config import Settings
from waht_agent.persistence.models import ApprovalModel, ConversationModel, MessageModel
from waht_agent.persistence.repository import AgentRepository
from waht_agent.schemas.agent import (
    Actor,
    AgentMessageView,
    AgentRunRequest,
    AgentTurnResponse,
    ApprovalDecision,
    ApprovalRequest,
    ApprovalView,
    ConversationDetail,
    ConversationSummary,
    DraftProposal,
    NoteSource,
    RunStatus,
)


class AgentService:
    """编排会话、模型运行和人工审批，不承载 HTTP 细节。"""

    def __init__(
        self,
        settings: Settings,
        repository: AgentRepository,
        runner: AgentRunner,
        core_client: WahtCoreClient,
    ) -> None:
        self._settings = settings
        self._repository = repository
        self._runner = runner
        self._core_client = core_client

    def create_conversation(self, actor: Actor, title: str | None) -> ConversationSummary:
        return self._to_summary(self._repository.create_conversation(actor.user_id, title))

    def list_conversations(self, actor: Actor) -> list[ConversationSummary]:
        return [self._to_summary(item) for item in self._repository.list_conversations(actor.user_id)]

    def get_conversation(self, conversation_id: str, actor: Actor) -> ConversationDetail:
        conversation = self._repository.get_conversation(conversation_id, actor.user_id)
        messages = self._repository.list_messages(conversation_id, actor.user_id)
        approval = self._repository.get_pending_approval(conversation_id, actor.user_id)
        return ConversationDetail(
            **self._to_summary(conversation).model_dump(),
            messages=[self._to_message(item) for item in messages],
            pending_approval=self._to_approval(approval) if approval else None,
        )

    async def run(self, conversation_id: str, request: AgentRunRequest) -> AgentTurnResponse:
        self._repository.get_conversation(conversation_id, request.actor.user_id)
        history = [
            self._to_message(item)
            for item in self._repository.list_messages(conversation_id, request.actor.user_id)
        ]
        self._repository.create_run(
            request.run_id,
            conversation_id,
            request.actor.user_id,
            self._settings.ai_model,
        )
        self._repository.add_message(
            conversation_id,
            request.actor.user_id,
            "USER",
            request.message,
        )
        try:
            execution = await self._runner.run(request.message, history, request.delegation_token)
            for record in execution.tool_records:
                self._repository.add_tool_call(
                    request.run_id,
                    record.name,
                    record.arguments,
                    record.result_summary,
                )

            sources = [item.model_dump(by_alias=True, mode="json") for item in execution.sources]
            self._repository.add_message(
                conversation_id,
                request.actor.user_id,
                "ASSISTANT",
                execution.answer,
                sources,
            )
            approval = None
            status = RunStatus.COMPLETED
            if execution.pending_draft:
                approval_model = self._repository.create_approval(
                    request.run_id,
                    "CREATE_NOTE_DRAFT",
                    execution.pending_draft.model_dump(by_alias=True),
                )
                approval = self._to_approval(approval_model)
                status = RunStatus.WAITING_APPROVAL
            self._repository.finish_run(
                request.run_id,
                status.value,
                execution.input_tokens,
                execution.output_tokens,
                execution.total_tokens,
            )
            return AgentTurnResponse(
                conversation_id=conversation_id,
                run_id=request.run_id,
                status=status,
                answer=execution.answer,
                sources=execution.sources,
                approval=approval,
            )
        except Exception as exc:
            self._repository.finish_run(request.run_id, RunStatus.FAILED.value, error_message=str(exc)[:1000])
            raise

    async def decide_approval(self, approval_id: str, request: ApprovalRequest) -> AgentTurnResponse:
        approval = self._repository.get_approval(approval_id, request.actor.user_id)
        run = self._repository.get_run_for_approval(approval_id, request.actor.user_id)
        if approval.status != "PENDING":
            return self._completed_approval_response(run.conversation_id, run.id, approval)

        stored_draft = DraftProposal.model_validate(approval.payload)
        if request.decision == ApprovalDecision.REJECT:
            decided = self._repository.decide_approval(
                approval_id,
                request.actor.user_id,
                "REJECTED",
                None,
                None,
            )
            self._repository.finish_run(run.id, RunStatus.CANCELLED.value)
            answer = "已取消创建笔记草稿。"
            self._repository.add_message(run.conversation_id, request.actor.user_id, "ASSISTANT", answer)
            return AgentTurnResponse(
                conversation_id=run.conversation_id,
                run_id=run.id,
                status=RunStatus.CANCELLED,
                answer=answer,
                sources=[],
                approval=self._to_approval(decided),
            )

        draft = request.draft if request.decision == ApprovalDecision.EDIT else stored_draft
        if draft is None:
            draft = stored_draft
        created = await self._core_client.create_note_draft(
            draft,
            request.delegation_token,
            approval_id,
        )
        decided = self._repository.decide_approval(
            approval_id,
            request.actor.user_id,
            "EDITED" if request.decision == ApprovalDecision.EDIT else "APPROVED",
            draft.model_dump(by_alias=True),
            created.id,
        )
        self._repository.finish_run(run.id, RunStatus.COMPLETED.value)
        answer = f"笔记草稿《{created.title}》已创建，可在笔记编辑器中继续完善。"
        self._repository.add_message(run.conversation_id, request.actor.user_id, "ASSISTANT", answer)
        return AgentTurnResponse(
            conversation_id=run.conversation_id,
            run_id=run.id,
            status=RunStatus.COMPLETED,
            answer=answer,
            sources=[],
            approval=self._to_approval(decided),
        )

    @staticmethod
    def _to_summary(model: ConversationModel) -> ConversationSummary:
        return ConversationSummary.model_validate(model)

    @staticmethod
    def _to_message(model: MessageModel) -> AgentMessageView:
        sources = [NoteSource.model_validate(item) for item in model.sources]
        return AgentMessageView(
            id=model.id,
            role=model.role,
            content=model.content,
            sources=sources,
            created_at=model.created_at,
        )

    @staticmethod
    def _to_approval(model: ApprovalModel) -> ApprovalView:
        payload = model.decided_payload or model.payload
        return ApprovalView(
            id=model.id,
            status=model.status,
            action=model.action,
            draft=DraftProposal.model_validate(payload),
            created_note_id=model.created_note_id,
        )

    def _completed_approval_response(
        self,
        conversation_id: str,
        run_id: str,
        approval: ApprovalModel,
    ) -> AgentTurnResponse:
        if approval.status == "REJECTED":
            status = RunStatus.CANCELLED
            answer = "该草稿审批已取消。"
        else:
            status = RunStatus.COMPLETED
            answer = "该草稿审批已经处理完成。"
        return AgentTurnResponse(
            conversation_id=conversation_id,
            run_id=run_id,
            status=status,
            answer=answer,
            sources=[],
            approval=self._to_approval(approval),
        )
