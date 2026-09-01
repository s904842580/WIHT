from dataclasses import dataclass, field

from waht_agent.clients.waht_core import WahtCoreClient
from waht_agent.schemas.agent import DraftProposal, NoteSource


@dataclass(slots=True)
class ToolExecutionRecord:
    """可持久化的工具调用摘要；不包含授权令牌或完整笔记正文。"""

    name: str
    arguments: dict[str, object]
    result_summary: dict[str, object]


@dataclass(slots=True)
class AgentRunContext:
    """单次 Agent 运行上下文，负责隔离用户授权与工具执行结果。"""

    core_client: WahtCoreClient
    delegation_token: str
    sources: list[NoteSource] = field(default_factory=list)
    tool_records: list[ToolExecutionRecord] = field(default_factory=list)
    pending_draft: DraftProposal | None = None

    def add_source(self, source: NoteSource) -> None:
        if all(item.note_id != source.note_id for item in self.sources):
            self.sources.append(source)
