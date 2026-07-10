package com.waht.platform.vo;

import com.waht.platform.entity.NoteCategoryEntity;

public class NoteCategoryResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;

    public static NoteCategoryResponse from(NoteCategoryEntity category) {
        if (category == null) {
            return null;
        }
        NoteCategoryResponse response = new NoteCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());
        response.setDescription(category.getDescription());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
