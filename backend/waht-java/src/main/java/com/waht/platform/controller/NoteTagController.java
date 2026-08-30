package com.waht.platform.controller;

import com.waht.platform.common.api.BaseResponse;
import com.waht.platform.service.NoteService;
import com.waht.platform.vo.NoteTagResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 笔记编辑和公开展示共用的有效标签查询接口。
 */
@RestController
@RequestMapping("/api/note-tags")
public class NoteTagController {

    private final NoteService noteService;

    public NoteTagController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public BaseResponse<List<NoteTagResponse>> listActiveTags() {
        return BaseResponse.success(noteService.listActiveTags());
    }
}
