package com.waht.platform.controller;

import com.waht.platform.common.api.BaseResponse;
import com.waht.platform.common.api.PageResponse;
import com.waht.platform.dto.PublishedNoteQueryRequest;
import com.waht.platform.service.NoteService;
import com.waht.platform.vo.NoteDetailResponse;
import com.waht.platform.vo.NoteSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 博客公开阅读接口，只暴露已发布的学习笔记。
 */
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public BaseResponse<PageResponse<NoteSummaryResponse>> listPublishedNotes(
            @Valid PublishedNoteQueryRequest query) {
        return BaseResponse.success(noteService.listPublishedNotes(query));
    }

    @GetMapping("/{slug}")
    public BaseResponse<NoteDetailResponse> getPublishedNote(@PathVariable String slug) {
        return BaseResponse.success(noteService.getPublishedNoteBySlug(slug));
    }
}
