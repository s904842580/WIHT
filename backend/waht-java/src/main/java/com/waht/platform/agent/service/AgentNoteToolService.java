package com.waht.platform.agent.service;

import com.waht.platform.agent.dto.AgentNoteDraftRequest;
import com.waht.platform.agent.dto.AgentNoteSearchRequest;
import com.waht.platform.agent.entity.AgentNoteDraftRequestEntity;
import com.waht.platform.agent.mapper.AgentNoteDraftRequestMapper;
import com.waht.platform.agent.vo.AgentCreatedNoteResponse;
import com.waht.platform.agent.vo.AgentNoteDetailResponse;
import com.waht.platform.agent.vo.AgentNoteMetadataResponse;
import com.waht.platform.agent.vo.AgentNoteSearchItemResponse;
import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.common.exception.ServiceException;
import com.waht.platform.dto.NoteSaveRequest;
import com.waht.platform.service.NoteService;
import com.waht.platform.vo.ManagedNoteSummaryResponse;
import com.waht.platform.vo.NoteCategoryResponse;
import com.waht.platform.vo.NoteEditorResponse;
import com.waht.platform.vo.NoteTagResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Python Agent 可调用的笔记能力适配层，复用 NoteService 的归属、分类、标签和草稿规则。
 */
@Service
public class AgentNoteToolService {

    private final NoteService noteService;
    private final AgentNoteDraftRequestMapper draftRequestMapper;

    public AgentNoteToolService(NoteService noteService, AgentNoteDraftRequestMapper draftRequestMapper) {
        this.noteService = noteService;
        this.draftRequestMapper = draftRequestMapper;
    }

    public List<AgentNoteSearchItemResponse> search(AgentNoteSearchRequest request, Long userId) {
        return noteService.searchMyNotes(
                        request.keyword(),
                        request.categoryId(),
                        request.tagIds(),
                        request.limit(),
                        userId)
                .stream()
                .map(this::toSearchItem)
                .toList();
    }

    public AgentNoteDetailResponse getNote(Long noteId, Long userId) {
        NoteEditorResponse note = noteService.getMyNote(noteId, userId);
        Map<Long, NoteCategoryResponse> categories = noteService.listActiveCategories().stream()
                .collect(Collectors.toMap(NoteCategoryResponse::getId, Function.identity()));
        Map<Long, NoteTagResponse> tags = noteService.listActiveTags().stream()
                .collect(Collectors.toMap(NoteTagResponse::getId, Function.identity()));
        NoteCategoryResponse category = categories.get(note.getCategoryId());
        List<NoteTagResponse> selectedTags = note.getTagIds().stream()
                .map(tags::get)
                .filter(item -> item != null)
                .toList();
        return new AgentNoteDetailResponse(
                note.getId(),
                note.getTitle(),
                note.getSlug(),
                note.getSummary(),
                note.getContent(),
                note.getStatus(),
                note.getCategoryId(),
                category == null ? null : category.getName(),
                note.getTagIds(),
                selectedTags.stream().map(NoteTagResponse::getName).toList(),
                note.getUpdatedAt()
        );
    }

    public AgentNoteMetadataResponse getMetadata() {
        return new AgentNoteMetadataResponse(
                noteService.listActiveCategories(),
                noteService.listActiveTags()
        );
    }

    /**
     * approvalId 同时作为幂等键；重复审批返回第一次创建的草稿，不会再插入一篇。
     */
    @Transactional
    public AgentCreatedNoteResponse createDraft(
            AgentNoteDraftRequest request,
            String idempotencyKey,
            Long userId) {
        AgentNoteDraftRequestEntity existing = draftRequestMapper.selectById(idempotencyKey);
        if (existing != null) {
            if (!userId.equals(existing.getUserId())) {
                throw new ServiceException(ErrorCode.CONFLICT, "幂等键已被其他用户使用");
            }
            return toCreatedResponse(noteService.getMyNote(existing.getNoteId(), userId));
        }

        NoteSaveRequest saveRequest = new NoteSaveRequest();
        saveRequest.setTitle(request.title());
        saveRequest.setSummary(request.summary());
        saveRequest.setContent(request.content());
        saveRequest.setCategoryId(request.categoryId());
        saveRequest.setTagIds(request.tagIds());
        NoteEditorResponse created = noteService.createNote(saveRequest, userId);

        AgentNoteDraftRequestEntity record = new AgentNoteDraftRequestEntity();
        record.setIdempotencyKey(idempotencyKey);
        record.setUserId(userId);
        record.setNoteId(created.getId());
        record.setCreatedAt(LocalDateTime.now());
        try {
            draftRequestMapper.insert(record);
        } catch (DuplicateKeyException ex) {
            throw new ServiceException(ErrorCode.CONFLICT, "草稿审批正在处理，请稍后重试");
        }
        return toCreatedResponse(created);
    }

    private AgentNoteSearchItemResponse toSearchItem(ManagedNoteSummaryResponse note) {
        return new AgentNoteSearchItemResponse(
                note.getId(),
                note.getTitle(),
                note.getSlug(),
                note.getSummary(),
                note.getStatus(),
                note.getCategory() == null ? null : note.getCategory().getName(),
                note.getTags().stream().map(NoteTagResponse::getName).toList(),
                note.getUpdatedAt()
        );
    }

    private AgentCreatedNoteResponse toCreatedResponse(NoteEditorResponse note) {
        return new AgentCreatedNoteResponse(note.getId(), note.getTitle(), note.getSlug(), note.getStatus());
    }
}
