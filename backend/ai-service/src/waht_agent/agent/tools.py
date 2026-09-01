import json

from agents import RunContextWrapper, function_tool

from waht_agent.agent.context import AgentRunContext, ToolExecutionRecord
from waht_agent.schemas.agent import DraftProposal, NoteSearchRequest, NoteSource


@function_tool
async def search_notes(
    context: RunContextWrapper[AgentRunContext],
    keyword: str,
    category_id: int | None = None,
    tag_ids: list[int] | None = None,
    limit: int = 5,
) -> str:
    """按关键词搜索当前用户的学习笔记，可选分类、标签和返回数量。"""

    request = NoteSearchRequest(
        keyword=keyword,
        category_id=category_id,
        tag_ids=tag_ids or [],
        limit=limit,
    )
    notes = await context.context.core_client.search_notes(request, context.context.delegation_token)
    for note in notes:
        context.context.add_source(
            NoteSource(note_id=note.id, title=note.title, slug=note.slug, status=note.status)
        )
    result = [note.model_dump(by_alias=True, mode="json") for note in notes]
    context.context.tool_records.append(
        ToolExecutionRecord(
            name="search_notes",
            arguments=request.model_dump(by_alias=True),
            result_summary={"count": len(notes), "noteIds": [note.id for note in notes]},
        )
    )
    return json.dumps(result, ensure_ascii=False)


@function_tool
async def get_note(context: RunContextWrapper[AgentRunContext], note_id: int) -> str:
    """读取当前用户的一篇完整学习笔记。"""

    note = await context.context.core_client.get_note(note_id, context.context.delegation_token)
    context.context.add_source(
        NoteSource(note_id=note.id, title=note.title, slug=note.slug, status=note.status)
    )
    context.context.tool_records.append(
        ToolExecutionRecord(
            name="get_note",
            arguments={"noteId": note_id},
            result_summary={"noteId": note.id, "title": note.title},
        )
    )
    return note.model_dump_json(by_alias=True)


@function_tool
async def list_note_metadata(context: RunContextWrapper[AgentRunContext]) -> str:
    """列出当前用户可选的笔记分类和标签。"""

    metadata = await context.context.core_client.get_note_metadata(context.context.delegation_token)
    context.context.tool_records.append(
        ToolExecutionRecord(
            name="list_note_metadata",
            arguments={},
            result_summary={
                "categoryCount": len(metadata.categories),
                "tagCount": len(metadata.tags),
            },
        )
    )
    return metadata.model_dump_json(by_alias=True)


@function_tool
async def create_note_draft(
    context: RunContextWrapper[AgentRunContext],
    title: str,
    summary: str,
    content: str,
    category_id: int,
    tag_ids: list[int] | None = None,
) -> str:
    """提出一份笔记草稿供用户审批；本工具不会直接写入笔记。"""

    draft = DraftProposal(
        title=title,
        summary=summary,
        content=content,
        category_id=category_id,
        tag_ids=tag_ids or [],
    )
    context.context.pending_draft = draft
    context.context.tool_records.append(
        ToolExecutionRecord(
            name="create_note_draft",
            arguments={
                "title": draft.title,
                "categoryId": draft.category_id,
                "tagIds": draft.tag_ids,
            },
            result_summary={"requiresApproval": True, "contentLength": len(draft.content)},
        )
    )
    return json.dumps(
        {"status": "WAITING_APPROVAL", "message": "草稿已生成，等待用户确认后写入"},
        ensure_ascii=False,
    )
