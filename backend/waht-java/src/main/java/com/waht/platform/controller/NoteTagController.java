package com.waht.platform.controller;

import com.waht.platform.common.api.ApiResponse;
import com.waht.platform.service.NoteService;
import com.waht.platform.vo.NoteTagResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/note-tags")
public class NoteTagController {

    private final NoteService noteService;

    public NoteTagController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public ApiResponse<List<NoteTagResponse>> listActiveTags() {
        return ApiResponse.success(noteService.listActiveTags());
    }
}
