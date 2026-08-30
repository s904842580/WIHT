package com.waht.platform.controller;

import com.waht.platform.common.api.BaseResponse;
import com.waht.platform.common.security.CurrentUser;
import com.waht.platform.common.security.LoginUser;
import com.waht.platform.dto.NoteSaveRequest;
import com.waht.platform.service.NoteService;
import com.waht.platform.vo.ManagedNoteSummaryResponse;
import com.waht.platform.vo.NoteEditorResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 当前登录用户的笔记工作台接口。
 *
 * <p>所有查询和修改都带上当前用户 ID，普通用户无法读取或修改其他作者的草稿。</p>
 */
@RestController
@RequestMapping("/api/my/notes")
public class MyNoteController {

    private final NoteService noteService;

    public MyNoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public BaseResponse<List<ManagedNoteSummaryResponse>> listMyNotes(@LoginUser CurrentUser currentUser) {
        return BaseResponse.success(noteService.listMyNotes(currentUser.userId()));
    }

    @GetMapping("/{noteId}")
    public BaseResponse<NoteEditorResponse> getMyNote(
            @PathVariable Long noteId,
            @LoginUser CurrentUser currentUser) {
        return BaseResponse.success(noteService.getMyNote(noteId, currentUser.userId()));
    }

    @PostMapping
    public BaseResponse<NoteEditorResponse> createNote(
            @Valid @RequestBody NoteSaveRequest request,
            @LoginUser CurrentUser currentUser) {
        return BaseResponse.success(noteService.createNote(request, currentUser.userId()));
    }

    @PutMapping("/{noteId}")
    public BaseResponse<NoteEditorResponse> updateNote(
            @PathVariable Long noteId,
            @Valid @RequestBody NoteSaveRequest request,
            @LoginUser CurrentUser currentUser) {
        return BaseResponse.success(noteService.updateNote(noteId, request, currentUser.userId()));
    }

    @PostMapping("/{noteId}/publish")
    public BaseResponse<NoteEditorResponse> publishNote(
            @PathVariable Long noteId,
            @LoginUser CurrentUser currentUser) {
        return BaseResponse.success(noteService.publishNote(noteId, currentUser.userId()));
    }

    @PostMapping("/{noteId}/draft")
    public BaseResponse<NoteEditorResponse> moveNoteToDraft(
            @PathVariable Long noteId,
            @LoginUser CurrentUser currentUser) {
        return BaseResponse.success(noteService.moveNoteToDraft(noteId, currentUser.userId()));
    }

    @DeleteMapping("/{noteId}")
    public BaseResponse<Void> deleteNote(
            @PathVariable Long noteId,
            @LoginUser CurrentUser currentUser) {
        noteService.deleteNote(noteId, currentUser.userId());
        return BaseResponse.success();
    }
}
