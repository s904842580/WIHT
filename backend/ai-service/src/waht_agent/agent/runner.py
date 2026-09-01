from dataclasses import dataclass, field
from typing import Protocol

from agents import Agent, Runner

from waht_agent.agent.context import AgentRunContext, ToolExecutionRecord
from waht_agent.agent.tools import create_note_draft, get_note, list_note_metadata, search_notes
from waht_agent.clients.waht_core import WahtCoreClient
from waht_agent.core.config import Settings
from waht_agent.core.errors import AgentUnavailableError
from waht_agent.schemas.agent import AgentMessageView, DraftProposal, NoteSource


@dataclass(slots=True)
class AgentExecutionResult:
    answer: str
    sources: list[NoteSource] = field(default_factory=list)
    pending_draft: DraftProposal | None = None
    tool_records: list[ToolExecutionRecord] = field(default_factory=list)
    input_tokens: int = 0
    output_tokens: int = 0
    total_tokens: int = 0


class AgentRunner(Protocol):
    async def run(
        self,
        message: str,
        history: list[AgentMessageView],
        delegation_token: str,
    ) -> AgentExecutionResult: ...


class OpenAIAgentRunner:
    """使用 OpenAI Agents SDK 执行一次完整、非流式的学习助手任务。"""

    def __init__(self, settings: Settings, core_client: WahtCoreClient) -> None:
        self._settings = settings
        self._core_client = core_client

    async def run(
        self,
        message: str,
        history: list[AgentMessageView],
        delegation_token: str,
    ) -> AgentExecutionResult:
        if not self._settings.openai_api_key or not self._settings.ai_model:
            raise AgentUnavailableError("请先配置 OPENAI_API_KEY 和 WAHT_AI_MODEL")

        context = AgentRunContext(
            core_client=self._core_client,
            delegation_token=delegation_token,
        )
        agent = Agent[AgentRunContext](
            name="WAHT 学习助手",
            model=self._settings.ai_model,
            instructions=(
                "你是 WAHT 作者工作台中的学习助手。使用中文回答，先理解问题，再按需调用工具。"
                "涉及用户已有笔记时必须先搜索或读取笔记，不得伪造来源。"
                "只有用户明确要求整理、生成或保存笔记时，才调用 create_note_draft。"
                "create_note_draft 只会产生待审批草稿，必须提醒用户确认，不能声称已经保存。"
                "回答应简洁、结构清晰，并指出使用了哪些已有笔记。"
            ),
            tools=[search_notes, get_note, list_note_metadata, create_note_draft],
        )
        input_text = self._build_input(history, message)
        try:
            result = await Runner.run(
                starting_agent=agent,
                input=input_text,
                context=context,
                max_turns=self._settings.max_agent_turns,
            )
        except Exception as exc:
            raise AgentUnavailableError("模型调用失败，请稍后重试") from exc

        answer = str(result.final_output).strip()
        if not answer:
            raise AgentUnavailableError("模型没有返回有效内容")
        usage = result.context_wrapper.usage
        return AgentExecutionResult(
            answer=answer,
            sources=context.sources,
            pending_draft=context.pending_draft,
            tool_records=context.tool_records,
            input_tokens=usage.input_tokens,
            output_tokens=usage.output_tokens,
            total_tokens=usage.total_tokens,
        )

    @staticmethod
    def _build_input(history: list[AgentMessageView], message: str) -> str:
        recent = history[-12:]
        lines = ["以下是当前对话的最近记录："]
        for item in recent:
            role = "用户" if item.role == "USER" else "助手"
            lines.append(f"{role}: {item.content}")
        lines.append(f"用户的新问题: {message}")
        return "\n".join(lines)
