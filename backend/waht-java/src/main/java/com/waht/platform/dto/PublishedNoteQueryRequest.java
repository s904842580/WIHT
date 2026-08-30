package com.waht.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 公开笔记列表的查询参数，限制页大小和关键词长度以保护公开接口。
 */
public class PublishedNoteQueryRequest {

    @Min(value = 1, message = "必须大于等于 1")
    private long page = 1;

    @Min(value = 1, message = "必须大于等于 1")
    @Max(value = 50, message = "不能超过 50")
    private long pageSize = 10;

    @Size(max = 100, message = "不能超过 100 个字符")
    private String keyword;

    @Positive(message = "必须为正整数")
    private Long categoryId;

    @Positive(message = "必须为正整数")
    private Long tagId;

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}
