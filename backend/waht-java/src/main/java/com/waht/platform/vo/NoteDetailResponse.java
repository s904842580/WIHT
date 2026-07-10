package com.waht.platform.vo;

import com.waht.platform.entity.NoteEntity;

import java.time.LocalDateTime;
import java.util.List;

public class NoteDetailResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private NoteCategoryResponse category;
    private List<NoteTagResponse> tags;
    private Long viewCount;
    private LocalDateTime publishedAt;

    public static NoteDetailResponse from(NoteEntity note, NoteCategoryResponse category, List<NoteTagResponse> tags) {
        NoteDetailResponse response = new NoteDetailResponse();
        response.setId(note.getId());
        response.setTitle(note.getTitle());
        response.setSlug(note.getSlug());
        response.setSummary(note.getSummary());
        response.setContent(note.getContent());
        response.setCategory(category);
        response.setTags(tags);
        response.setViewCount(note.getViewCount());
        response.setPublishedAt(note.getPublishedAt());
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

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
