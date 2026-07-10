package com.waht.platform.vo;

import com.waht.platform.entity.ProjectLinkEntity;

public class ProjectLinkResponse {

    private Long id;
    private String linkType;
    private String title;
    private String url;
    private Integer sortOrder;

    public static ProjectLinkResponse from(ProjectLinkEntity link) {
        ProjectLinkResponse response = new ProjectLinkResponse();
        response.setId(link.getId());
        response.setLinkType(link.getLinkType());
        response.setTitle(link.getTitle());
        response.setUrl(link.getUrl());
        response.setSortOrder(link.getSortOrder());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
