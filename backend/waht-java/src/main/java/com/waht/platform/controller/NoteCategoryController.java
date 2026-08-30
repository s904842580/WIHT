package com.waht.platform.controller;

import com.waht.platform.common.api.BaseResponse;
import com.waht.platform.service.NoteService;
import com.waht.platform.vo.NoteCategoryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 笔记编辑和公开筛选共用的有效分类查询接口。
 */
@RestController
@RequestMapping("/api/note-categories")
public class NoteCategoryController {

    private final NoteService noteService;

    public NoteCategoryController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public BaseResponse<List<NoteCategoryResponse>> listActiveCategories() {
        return BaseResponse.success(noteService.listActiveCategories());
    }
}
