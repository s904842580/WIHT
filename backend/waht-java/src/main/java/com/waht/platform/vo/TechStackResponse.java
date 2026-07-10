package com.waht.platform.vo;

import com.waht.platform.entity.TechStackEntity;

public class TechStackResponse {

    private Long id;
    private String name;
    private String slug;
    private String techType;
    private String iconUrl;

    public static TechStackResponse from(TechStackEntity techStack) {
        TechStackResponse response = new TechStackResponse();
        response.setId(techStack.getId());
        response.setName(techStack.getName());
        response.setSlug(techStack.getSlug());
        response.setTechType(techStack.getTechType());
        response.setIconUrl(techStack.getIconUrl());
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

    public String getTechType() {
        return techType;
    }

    public void setTechType(String techType) {
        this.techType = techType;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
