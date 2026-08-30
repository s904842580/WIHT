package com.waht.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waht.platform.common.api.PageResponse;
import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.common.exception.ServiceException;
import com.waht.platform.common.model.NoteStatus;
import com.waht.platform.dto.NoteSaveRequest;
import com.waht.platform.dto.PublishedNoteQueryRequest;
import com.waht.platform.entity.NoteCategoryEntity;
import com.waht.platform.entity.NoteEntity;
import com.waht.platform.entity.NoteTagEntity;
import com.waht.platform.entity.NoteTagRelEntity;
import com.waht.platform.mapper.NoteCategoryMapper;
import com.waht.platform.mapper.NoteMapper;
import com.waht.platform.mapper.NoteTagMapper;
import com.waht.platform.mapper.NoteTagRelMapper;
import com.waht.platform.vo.ManagedNoteSummaryResponse;
import com.waht.platform.vo.NoteCategoryResponse;
import com.waht.platform.vo.NoteDetailResponse;
import com.waht.platform.vo.NoteEditorResponse;
import com.waht.platform.vo.NoteSummaryResponse;
import com.waht.platform.vo.NoteTagResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 学习笔记领域服务。
 *
 * <p>公开阅读、作者工作台和状态流转都在这里执行同一套分类、标签、slug 与归属校验，
 * 控制器只负责 HTTP 参数转换，Mapper 只负责数据访问。</p>
 */
@Service
public class NoteService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final DateTimeFormatter SLUG_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final NoteMapper noteMapper;
    private final NoteCategoryMapper noteCategoryMapper;
    private final NoteTagMapper noteTagMapper;
    private final NoteTagRelMapper noteTagRelMapper;

    public NoteService(
            NoteMapper noteMapper,
            NoteCategoryMapper noteCategoryMapper,
            NoteTagMapper noteTagMapper,
            NoteTagRelMapper noteTagRelMapper) {
        this.noteMapper = noteMapper;
        this.noteCategoryMapper = noteCategoryMapper;
        this.noteTagMapper = noteTagMapper;
        this.noteTagRelMapper = noteTagRelMapper;
    }

    /**
     * 分页查询博客公开列表，草稿和隐藏笔记不会返回。
     *
     * <p>关键词匹配标题和摘要；分类直接过滤笔记字段；标签通过关联表 EXISTS 查询，
     * 避免 JOIN 导致一篇笔记因多个标签产生重复行。</p>
     */
    public PageResponse<NoteSummaryResponse> listPublishedNotes(PublishedNoteQueryRequest query) {
        String keyword = normalizeNullable(query.getKeyword());
        LambdaQueryWrapper<NoteEntity> wrapper = new LambdaQueryWrapper<NoteEntity>()
                .eq(NoteEntity::getStatus, NoteStatus.PUBLISHED.name())
                .and(StringUtils.hasText(keyword), condition -> condition
                        .like(NoteEntity::getTitle, keyword)
                        .or()
                        .like(NoteEntity::getSummary, keyword))
                .eq(query.getCategoryId() != null, NoteEntity::getCategoryId, query.getCategoryId())
                .apply(query.getTagId() != null,
                        "EXISTS (SELECT 1 FROM waht_note_tag_rel rel "
                                + "WHERE rel.note_id = waht_note.id AND rel.tag_id = {0})",
                        query.getTagId())
                .orderByDesc(NoteEntity::getPublishedAt)
                .orderByDesc(NoteEntity::getId);

        Page<NoteEntity> notePage = noteMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()),
                wrapper);
        List<NoteEntity> notes = notePage.getRecords();
        if (notes.isEmpty()) {
            return PageResponse.of(List.of(), notePage.getCurrent(), notePage.getSize(), notePage.getTotal());
        }

        Map<Long, NoteCategoryResponse> categoryMap = loadCategoryMap(notes);
        Map<Long, List<NoteTagResponse>> noteTagMap = loadNoteTagMap(notes);
        List<NoteSummaryResponse> items = notes.stream()
                .map(note -> NoteSummaryResponse.from(
                        note,
                        categoryMap.get(note.getCategoryId()),
                        noteTagMap.getOrDefault(note.getId(), List.of())))
                .toList();
        return PageResponse.of(items, notePage.getCurrent(), notePage.getSize(), notePage.getTotal());
    }

    /**
     * 按公开 slug 查询详情，并用单条原子 SQL 累加浏览次数。
     */
    @Transactional
    public NoteDetailResponse getPublishedNoteBySlug(String slug) {
        NoteEntity note = noteMapper.selectOne(new LambdaQueryWrapper<NoteEntity>()
                .eq(NoteEntity::getSlug, slug)
                .eq(NoteEntity::getStatus, NoteStatus.PUBLISHED.name())
                .last("LIMIT 1"));
        if (note == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "笔记不存在");
        }

        if (noteMapper.incrementViewCount(note.getId()) > 0) {
            note.setViewCount((note.getViewCount() == null ? 0L : note.getViewCount()) + 1L);
        }
        NoteCategoryResponse category = NoteCategoryResponse.from(noteCategoryMapper.selectById(note.getCategoryId()));
        List<NoteTagResponse> tags = loadNoteTagMap(List.of(note)).getOrDefault(note.getId(), List.of());
        return NoteDetailResponse.from(note, category, tags);
    }

    /**
     * 查询当前作者全部笔记，包括尚未发布的草稿。
     */
    public List<ManagedNoteSummaryResponse> listMyNotes(Long userId) {
        List<NoteEntity> notes = noteMapper.selectList(new LambdaQueryWrapper<NoteEntity>()
                .eq(NoteEntity::getCreatedBy, userId)
                .orderByDesc(NoteEntity::getUpdatedAt)
                .orderByDesc(NoteEntity::getId));
        if (notes.isEmpty()) {
            return List.of();
        }

        Map<Long, NoteCategoryResponse> categoryMap = loadCategoryMap(notes);
        Map<Long, List<NoteTagResponse>> noteTagMap = loadNoteTagMap(notes);
        return notes.stream()
                .map(note -> ManagedNoteSummaryResponse.from(
                        note,
                        categoryMap.get(note.getCategoryId()),
                        noteTagMap.getOrDefault(note.getId(), List.of())))
                .toList();
    }

    public NoteEditorResponse getMyNote(Long noteId, Long userId) {
        NoteEntity note = getOwnedNote(noteId, userId);
        return toEditorResponse(note);
    }

    /**
     * 新建笔记固定保存为草稿，发布动作使用独立接口以避免误公开未完成内容。
     */
    @Transactional
    public NoteEditorResponse createNote(NoteSaveRequest request, Long userId) {
        validateActiveCategory(request.getCategoryId());
        List<Long> tagIds = validateActiveTags(request.getTagIds());
        String slug = resolveCreateSlug(request.getSlug());
        ensureSlugAvailable(slug, null);

        NoteEntity note = new NoteEntity();
        applyEditableFields(note, request, slug);
        note.setStatus(NoteStatus.DRAFT.name());
        note.setViewCount(0L);
        note.setCreatedBy(userId);
        note.setUpdatedBy(userId);

        try {
            noteMapper.insert(note);
        } catch (DuplicateKeyException ex) {
            throw new ServiceException(ErrorCode.CONFLICT, "笔记访问标识已存在");
        }
        syncTags(note.getId(), tagIds);
        return toEditorResponse(getOwnedNote(note.getId(), userId));
    }

    @Transactional
    public NoteEditorResponse updateNote(Long noteId, NoteSaveRequest request, Long userId) {
        NoteEntity note = getOwnedNote(noteId, userId);
        if (NoteStatus.PUBLISHED.name().equals(note.getStatus()) && !StringUtils.hasText(request.getContent())) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "已发布笔记的正文不能为空");
        }
        validateActiveCategory(request.getCategoryId());
        List<Long> tagIds = validateActiveTags(request.getTagIds());
        String slug = StringUtils.hasText(request.getSlug())
                ? request.getSlug().trim().toLowerCase(Locale.ROOT)
                : note.getSlug();
        ensureSlugAvailable(slug, noteId);

        try {
            noteMapper.update(null, new LambdaUpdateWrapper<NoteEntity>()
                    .eq(NoteEntity::getId, noteId)
                    .eq(NoteEntity::getCreatedBy, userId)
                    .set(NoteEntity::getTitle, request.getTitle().trim())
                    .set(NoteEntity::getSlug, slug)
                    .set(NoteEntity::getSummary, normalizeNullable(request.getSummary()))
                    .set(NoteEntity::getContent, request.getContent())
                    .set(NoteEntity::getCategoryId, request.getCategoryId())
                    .set(NoteEntity::getUpdatedBy, userId));
        } catch (DuplicateKeyException ex) {
            throw new ServiceException(ErrorCode.CONFLICT, "笔记访问标识已存在");
        }
        syncTags(noteId, tagIds);
        return toEditorResponse(getOwnedNote(noteId, userId));
    }

    @Transactional
    public NoteEditorResponse publishNote(Long noteId, Long userId) {
        NoteEntity note = getOwnedNote(noteId, userId);
        if (!StringUtils.hasText(note.getContent())) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "正文不能为空，无法发布");
        }
        validateActiveCategory(note.getCategoryId());

        if (!NoteStatus.PUBLISHED.name().equals(note.getStatus())) {
            LocalDateTime publishedAt = LocalDateTime.now();
            noteMapper.update(null, new LambdaUpdateWrapper<NoteEntity>()
                    .eq(NoteEntity::getId, noteId)
                    .eq(NoteEntity::getCreatedBy, userId)
                    .set(NoteEntity::getStatus, NoteStatus.PUBLISHED.name())
                    .set(NoteEntity::getPublishedAt, publishedAt)
                    .set(NoteEntity::getUpdatedBy, userId));
            note.setStatus(NoteStatus.PUBLISHED.name());
            note.setPublishedAt(publishedAt);
        }
        return toEditorResponse(getOwnedNote(noteId, userId));
    }

    @Transactional
    public NoteEditorResponse moveNoteToDraft(Long noteId, Long userId) {
        NoteEntity note = getOwnedNote(noteId, userId);
        if (!NoteStatus.DRAFT.name().equals(note.getStatus())) {
            noteMapper.update(null, new LambdaUpdateWrapper<NoteEntity>()
                    .eq(NoteEntity::getId, noteId)
                    .eq(NoteEntity::getCreatedBy, userId)
                    .set(NoteEntity::getStatus, NoteStatus.DRAFT.name())
                    .set(NoteEntity::getPublishedAt, null)
                    .set(NoteEntity::getUpdatedBy, userId));
        }
        return toEditorResponse(getOwnedNote(noteId, userId));
    }

    @Transactional
    public void deleteNote(Long noteId, Long userId) {
        getOwnedNote(noteId, userId);
        noteTagRelMapper.deleteByNoteId(noteId);
        noteMapper.deleteById(noteId);
    }

    public List<NoteCategoryResponse> listActiveCategories() {
        return noteCategoryMapper.selectList(new LambdaQueryWrapper<NoteCategoryEntity>()
                        .eq(NoteCategoryEntity::getStatus, ACTIVE_STATUS)
                        .orderByAsc(NoteCategoryEntity::getSortOrder)
                        .orderByAsc(NoteCategoryEntity::getId))
                .stream()
                .map(NoteCategoryResponse::from)
                .toList();
    }

    public List<NoteTagResponse> listActiveTags() {
        return noteTagMapper.selectList(new LambdaQueryWrapper<NoteTagEntity>()
                        .eq(NoteTagEntity::getStatus, ACTIVE_STATUS)
                        .orderByAsc(NoteTagEntity::getId))
                .stream()
                .map(NoteTagResponse::from)
                .toList();
    }

    private NoteEntity getOwnedNote(Long noteId, Long userId) {
        NoteEntity note = noteMapper.selectOne(new LambdaQueryWrapper<NoteEntity>()
                .eq(NoteEntity::getId, noteId)
                .eq(NoteEntity::getCreatedBy, userId)
                .last("LIMIT 1"));
        if (note == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "笔记不存在或无权操作");
        }
        return note;
    }

    private void applyEditableFields(NoteEntity note, NoteSaveRequest request, String slug) {
        note.setTitle(request.getTitle().trim());
        note.setSlug(slug);
        note.setSummary(normalizeNullable(request.getSummary()));
        note.setContent(request.getContent());
        note.setCategoryId(request.getCategoryId());
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String resolveCreateSlug(String requestedSlug) {
        if (StringUtils.hasText(requestedSlug)) {
            return requestedSlug.trim().toLowerCase(Locale.ROOT);
        }
        String timePart = LocalDateTime.now().format(SLUG_TIME_FORMATTER);
        String randomPart = UUID.randomUUID().toString().substring(0, 8);
        return "note-" + timePart + "-" + randomPart;
    }

    private void ensureSlugAvailable(String slug, Long ignoredNoteId) {
        LambdaQueryWrapper<NoteEntity> wrapper = new LambdaQueryWrapper<NoteEntity>()
                .eq(NoteEntity::getSlug, slug);
        if (ignoredNoteId != null) {
            wrapper.ne(NoteEntity::getId, ignoredNoteId);
        }
        if (noteMapper.selectCount(wrapper) > 0) {
            throw new ServiceException(ErrorCode.CONFLICT, "笔记访问标识已存在");
        }
    }

    private void validateActiveCategory(Long categoryId) {
        NoteCategoryEntity category = noteCategoryMapper.selectById(categoryId);
        if (category == null || !ACTIVE_STATUS.equals(category.getStatus())) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "请选择有效的笔记分类");
        }
    }

    private List<Long> validateActiveTags(List<Long> requestedTagIds) {
        if (requestedTagIds == null || requestedTagIds.isEmpty()) {
            return List.of();
        }
        List<Long> tagIds = new ArrayList<>(new LinkedHashSet<>(requestedTagIds));
        Set<Long> activeTagIds = noteTagMapper.selectBatchIds(tagIds).stream()
                .filter(tag -> ACTIVE_STATUS.equals(tag.getStatus()))
                .map(NoteTagEntity::getId)
                .collect(Collectors.toSet());
        if (activeTagIds.size() != tagIds.size()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "存在无效的笔记标签");
        }
        return tagIds;
    }

    private void syncTags(Long noteId, List<Long> tagIds) {
        noteTagRelMapper.deleteByNoteId(noteId);
        if (!tagIds.isEmpty()) {
            noteTagRelMapper.insertBatch(noteId, tagIds);
        }
    }

    private NoteEditorResponse toEditorResponse(NoteEntity note) {
        List<Long> tagIds = noteTagRelMapper.selectByNoteIds(List.of(note.getId())).stream()
                .map(NoteTagRelEntity::getTagId)
                .toList();
        return NoteEditorResponse.from(note, tagIds);
    }

    private Map<Long, NoteCategoryResponse> loadCategoryMap(List<NoteEntity> notes) {
        Set<Long> categoryIds = notes.stream()
                .map(NoteEntity::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Map.of();
        }

        return noteCategoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(
                        NoteCategoryEntity::getId,
                        NoteCategoryResponse::from,
                        (oldValue, newValue) -> oldValue));
    }

    private Map<Long, List<NoteTagResponse>> loadNoteTagMap(List<NoteEntity> notes) {
        List<Long> noteIds = notes.stream().map(NoteEntity::getId).toList();
        if (noteIds.isEmpty()) {
            return Map.of();
        }

        List<NoteTagRelEntity> relations = noteTagRelMapper.selectByNoteIds(noteIds);
        if (relations.isEmpty()) {
            return Map.of();
        }

        Set<Long> tagIds = relations.stream()
                .map(NoteTagRelEntity::getTagId)
                .collect(Collectors.toSet());
        Map<Long, NoteTagResponse> tagMap = noteTagMapper.selectBatchIds(tagIds).stream()
                .filter(tag -> ACTIVE_STATUS.equals(tag.getStatus()))
                .collect(Collectors.toMap(
                        NoteTagEntity::getId,
                        NoteTagResponse::from,
                        (oldValue, newValue) -> oldValue));

        Map<Long, List<NoteTagResponse>> result = new LinkedHashMap<>();
        for (NoteTagRelEntity relation : relations) {
            NoteTagResponse tag = tagMap.get(relation.getTagId());
            if (tag != null) {
                result.computeIfAbsent(relation.getNoteId(), key -> new ArrayList<>()).add(tag);
            }
        }

        Map<Long, List<NoteTagResponse>> readonlyResult = new HashMap<>();
        result.forEach((noteId, tags) -> readonlyResult.put(noteId, Collections.unmodifiableList(tags)));
        return readonlyResult;
    }
}
