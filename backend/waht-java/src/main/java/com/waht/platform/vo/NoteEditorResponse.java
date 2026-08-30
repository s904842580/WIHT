package com.waht.platform.vo;

import com.waht.platform.entity.NoteEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记编辑器读取和保存后返回的完整数据，使用 ID 直接回填分类与标签控件。
 */
public class NoteEditorResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private Long categoryId;
    private List<Long> tagIds;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NoteEditorResponse from(NoteEntity note, List<Long> tagIds) {
        NoteEditorResponse response = new NoteEditorResponse();
        response.setId(note.getId());
        response.setTitle(note.getTitle());
        response.setSlug(note.getSlug());
        response.setSummary(note.getSummary());
        response.setContent(note.getContent());
        response.setCategoryId(note.getCategoryId());
        response.setTagIds(tagIds);
        response.setStatus(note.getStatus());
        response.setPublishedAt(note.getPublishedAt());
        response.setCreatedAt(note.getCreatedAt());
        response.setUpdatedAt(note.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
