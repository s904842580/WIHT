package com.waht.platform.vo;

import com.waht.platform.entity.NoteEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * “我的笔记”列表项，除公开信息外还包含草稿状态和最后更新时间。
 */
public class ManagedNoteSummaryResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String status;
    private NoteCategoryResponse category;
    private List<NoteTagResponse> tags;
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt;

    public static ManagedNoteSummaryResponse from(
            NoteEntity note,
            NoteCategoryResponse category,
            List<NoteTagResponse> tags) {
        ManagedNoteSummaryResponse response = new ManagedNoteSummaryResponse();
        response.setId(note.getId());
        response.setTitle(note.getTitle());
        response.setSlug(note.getSlug());
        response.setSummary(note.getSummary());
        response.setStatus(note.getStatus());
        response.setCategory(category);
        response.setTags(tags);
        response.setPublishedAt(note.getPublishedAt());
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public NoteCategoryResponse getCategory() {
        return category;
    }

    public void setCategory(NoteCategoryResponse category) {
        this.category = category;
    }

    public List<NoteTagResponse> getTags() {
        return tags;
    }

    public void setTags(List<NoteTagResponse> tags) {
        this.tags = tags;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
