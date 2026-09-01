package com.waht.platform.agent.controller;

import com.waht.platform.agent.dto.AgentNoteDraftRequest;
import com.waht.platform.agent.dto.AgentNoteSearchRequest;
import com.waht.platform.agent.security.AgentUser;
import com.waht.platform.agent.service.AgentNoteToolService;
import com.waht.platform.agent.vo.AgentCreatedNoteResponse;
import com.waht.platform.agent.vo.AgentNoteDetailResponse;
import com.waht.platform.agent.vo.AgentNoteMetadataResponse;
import com.waht.platform.agent.vo.AgentNoteSearchItemResponse;
import com.waht.platform.common.api.BaseResponse;
import com.waht.platform.common.security.AgentDelegation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仅供 Python Agent 调用的内部笔记工具，受共享令牌和 delegation JWT 双重保护。
 */
@Validated
@RestController
@RequestMapping("/api/internal/agent-tools")
public class AgentNoteToolController {

    private final AgentNoteToolService agentNoteToolService;

    public AgentNoteToolController(AgentNoteToolService agentNoteToolService) {
        this.agentNoteToolService = agentNoteToolService;
    }

    @PostMapping("/notes/search")
    public BaseResponse<List<AgentNoteSearchItemResponse>> searchNotes(
            @Valid @RequestBody AgentNoteSearchRequest request,
            @AgentUser AgentDelegation delegation) {
        return BaseResponse.success(agentNoteToolService.search(request, delegation.userId()));
    }

    @GetMapping("/notes/{noteId}")
    public BaseResponse<AgentNoteDetailResponse> getNote(
            @PathVariable Long noteId,
            @AgentUser AgentDelegation delegation) {
        return BaseResponse.success(agentNoteToolService.getNote(noteId, delegation.userId()));
    }

    @GetMapping("/note-metadata")
    public BaseResponse<AgentNoteMetadataResponse> getMetadata(@AgentUser AgentDelegation delegation) {
        return BaseResponse.success(agentNoteToolService.getMetadata());
    }

    @PostMapping("/note-drafts")
    public BaseResponse<AgentCreatedNoteResponse> createDraft(
            @Valid @RequestBody AgentNoteDraftRequest request,
            @RequestHeader("Idempotency-Key")
            @NotBlank @Size(max = 64) String idempotencyKey,
            @AgentUser AgentDelegation delegation) {
        return BaseResponse.success(agentNoteToolService.createDraft(request, idempotencyKey, delegation.userId()));
    }
}
