package com.waht.platform.agent.vo;

/**
 * Agent 草稿实际写入后的最小回执。
 */
public record AgentCreatedNoteResponse(Long id, String title, String slug, String status) {
}
