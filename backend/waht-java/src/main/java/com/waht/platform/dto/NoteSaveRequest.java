package com.waht.platform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * 新建和编辑笔记共用的请求对象。
 *
 * <p>正文允许为空以支持尽早保存草稿；真正发布时 Service 会再次校验正文内容。
 * 正文上限用于阻止异常大的请求占用应用和数据库资源。</p>
 */
public class NoteSaveRequest {

    @NotBlank(message = "不能为空")
    @Size(max = 120, message = "长度不能超过 120")
    private String title;

    @Size(max = 150, message = "长度不能超过 150")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "只能包含小写字母、数字和连字符")
    private String slug;

    @Size(max = 500, message = "长度不能超过 500")
    private String summary;

    @NotNull(message = "不能为空")
    @Size(max = 200000, message = "长度不能超过 200000")
    private String content;

    @NotNull(message = "不能为空")
    @Positive(message = "必须为正整数")
    private Long categoryId;

    @Valid
    @Size(max = 10, message = "最多选择 10 个标签")
    private List<
            @NotNull(message = "不能包含空值")
            @Positive(message = "必须为正整数") Long> tagIds = new ArrayList<>();

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
        this.tagIds = tagIds == null ? new ArrayList<>() : tagIds;
    }
}