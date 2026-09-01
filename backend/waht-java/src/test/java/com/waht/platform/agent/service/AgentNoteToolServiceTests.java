package com.waht.platform.agent.service;

import com.waht.platform.agent.dto.AgentNoteDraftRequest;
import com.waht.platform.agent.entity.AgentNoteDraftRequestEntity;
import com.waht.platform.agent.mapper.AgentNoteDraftRequestMapper;
import com.waht.platform.agent.vo.AgentCreatedNoteResponse;
import com.waht.platform.service.NoteService;
import com.waht.platform.vo.NoteEditorResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Agent 草稿工具测试，重点验证审批重试不会重复创建笔记。
 */
class AgentNoteToolServiceTests {

    private final NoteService noteService = mock(NoteService.class);
    private final AgentNoteDraftRequestMapper draftRequestMapper = mock(AgentNoteDraftRequestMapper.class);
    private final AgentNoteToolService service = new AgentNoteToolService(noteService, draftRequestMapper);

    @Test
    void createDraftShouldReturnExistingNoteForRepeatedApproval() {
        AgentNoteDraftRequestEntity existing = new AgentNoteDraftRequestEntity();
        existing.setIdempotencyKey("approval-001");
        existing.setUserId(7L);
        existing.setNoteId(101L);
        when(draftRequestMapper.selectById("approval-001")).thenReturn(existing);
        when(noteService.getMyNote(101L, 7L)).thenReturn(note(101L));

        AgentCreatedNoteResponse response = service.createDraft(request(), "approval-001", 7L);

        assertEquals(101L, response.id());
        verify(noteService, never()).createNote(any(), any());
        verify(draftRequestMapper, never()).insert(any(AgentNoteDraftRequestEntity.class));
    }

    @Test
    void createDraftShouldUseNoteServiceAndRecordIdempotencyKey() {
        when(draftRequestMapper.selectById("approval-002")).thenReturn(null);
        when(noteService.createNote(any(), any())).thenReturn(note(102L));

        AgentCreatedNoteResponse response = service.createDraft(request(), "approval-002", 7L);

        assertEquals("DRAFT", response.status());
        verify(noteService).createNote(any(), any());
        verify(draftRequestMapper).insert(any(AgentNoteDraftRequestEntity.class));
    }

    private AgentNoteDraftRequest request() {
        return new AgentNoteDraftRequest(
                "Agent 学习笔记",
                "待审批摘要",
                "# 正文",
                1L,
                List.of(2L)
        );
    }

    private NoteEditorResponse note(Long id) {
        NoteEditorResponse note = new NoteEditorResponse();
        note.setId(id);
        note.setTitle("Agent 学习笔记");
        note.setSlug("agent-note");
        note.setStatus("DRAFT");
        return note;
    }
}
