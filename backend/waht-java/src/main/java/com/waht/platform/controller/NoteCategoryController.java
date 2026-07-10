package com.waht.platform.controller;

import com.waht.platform.common.api.ApiResponse;
import com.waht.platform.service.NoteService;
import com.waht.platform.vo.NoteCategoryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/note-categories")
public class NoteCategoryController {

    private final NoteService noteService;

    public NoteCategoryController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public ApiResponse<List<NoteCategoryResponse>> listActiveCategories() {
        return ApiResponse.success(noteService.listActiveCategories());
    }
}
