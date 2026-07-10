package com.waht.platform.controller;

import com.waht.platform.common.api.ApiResponse;
import com.waht.platform.service.NoteService;
import com.waht.platform.vo.NoteDetailResponse;
import com.waht.platform.vo.NoteSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public ApiResponse<List<NoteSummaryResponse>> listPublishedNotes() {
        return ApiResponse.success(noteService.listPublishedNotes());
    }

    @GetMapping("/{slug}")
    public ApiResponse<NoteDetailResponse> getPublishedNote(@PathVariable String slug) {
        return ApiResponse.success(noteService.getPublishedNoteBySlug(slug));
    }
}
