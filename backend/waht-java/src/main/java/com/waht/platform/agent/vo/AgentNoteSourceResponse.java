package com.waht.platform.agent.vo;

/**
 * Agent 回答引用的笔记来源，前端可跳转到对应阅读页或编辑页。
 */
public record AgentNoteSourceResponse(Long noteId, String title, String slug, String status) {
}
