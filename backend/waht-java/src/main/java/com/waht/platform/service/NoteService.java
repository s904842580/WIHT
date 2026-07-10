package com.waht.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waht.platform.common.exception.BusinessException;
import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.entity.NoteCategoryEntity;
import com.waht.platform.entity.NoteEntity;
import com.waht.platform.entity.NoteTagEntity;
import com.waht.platform.entity.NoteTagRelEntity;
import com.waht.platform.mapper.NoteCategoryMapper;
import com.waht.platform.mapper.NoteMapper;
import com.waht.platform.mapper.NoteTagMapper;
import com.waht.platform.mapper.NoteTagRelMapper;
import com.waht.platform.vo.NoteCategoryResponse;
import com.waht.platform.vo.NoteDetailResponse;
import com.waht.platform.vo.NoteSummaryResponse;
import com.waht.platform.vo.NoteTagResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String PUBLISHED_STATUS = "PUBLISHED";

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

    public List<NoteSummaryResponse> listPublishedNotes() {
        List<NoteEntity> notes = noteMapper.selectList(new LambdaQueryWrapper<NoteEntity>()
                .eq(NoteEntity::getStatus, PUBLISHED_STATUS)
                .orderByDesc(NoteEntity::getPublishedAt)
                .orderByDesc(NoteEntity::getId));
        if (notes.isEmpty()) {
            return List.of();
        }

        Map<Long, NoteCategoryResponse> categoryMap = loadCategoryMap(notes);
        Map<Long, List<NoteTagResponse>> noteTagMap = loadNoteTagMap(notes);

        return notes.stream()
                .map(note -> NoteSummaryResponse.from(
                        note,
                        categoryMap.get(note.getCategoryId()),
                        noteTagMap.getOrDefault(note.getId(), List.of())))
                .toList();
    }

    public NoteDetailResponse getPublishedNoteBySlug(String slug) {
        NoteEntity note = noteMapper.selectOne(new LambdaQueryWrapper<NoteEntity>()
                .eq(NoteEntity::getSlug, slug)
                .eq(NoteEntity::getStatus, PUBLISHED_STATUS)
                .last("LIMIT 1"));
        if (note == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "笔记不存在");
        }

        NoteCategoryResponse category = null;
        if (note.getCategoryId() != null) {
            category = NoteCategoryResponse.from(noteCategoryMapper.selectById(note.getCategoryId()));
        }

        List<NoteTagResponse> tags = loadNoteTagMap(List.of(note)).getOrDefault(note.getId(), List.of());
        return NoteDetailResponse.from(note, category, tags);
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
            if (tag == null) {
                continue;
            }
            result.computeIfAbsent(relation.getNoteId(), key -> new ArrayList<>()).add(tag);
        }

        Map<Long, List<NoteTagResponse>> readonlyResult = new HashMap<>();
        result.forEach((noteId, tags) -> readonlyResult.put(noteId, Collections.unmodifiableList(tags)));
        return readonlyResult;
    }
}
