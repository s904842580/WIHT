package com.waht.platform.vo;

import com.waht.platform.entity.ProjectEntity;

import java.time.LocalDate;
import java.util.List;

public class ProjectSummaryResponse {

    private Long id;
    private String name;
    private String slug;
    private String summary;
    private Long coverAssetId;
    private Integer sortOrder;
    private LocalDate startedAt;
    private LocalDate endedAt;
    private List<TechStackResponse> techStacks;
    private List<ProjectLinkResponse> links;

    public static ProjectSummaryResponse from(
            ProjectEntity project,
            List<TechStackResponse> techStacks,
            List<ProjectLinkResponse> links) {
        ProjectSummaryResponse response = new ProjectSummaryResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setSlug(project.getSlug());
        response.setSummary(project.getSummary());
        response.setCoverAssetId(project.getCoverAssetId());
        response.setSortOrder(project.getSortOrder());
        response.setStartedAt(project.getStartedAt());
        response.setEndedAt(project.getEndedAt());
        response.setTechStacks(techStacks);
        response.setLinks(links);
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getCoverAssetId() {
        return coverAssetId;
    }

    public void setCoverAssetId(Long coverAssetId) {
        this.coverAssetId = coverAssetId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDate getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDate startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDate getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDate endedAt) {
        this.endedAt = endedAt;
    }

    public List<TechStackResponse> getTechStacks() {
        return techStacks;
    }

    public void setTechStacks(List<TechStackResponse> techStacks) {
        this.techStacks = techStacks;
    }

    public List<ProjectLinkResponse> getLinks() {
        return links;
    }

    public void setLinks(List<ProjectLinkResponse> links) {
        this.links = links;
    }
}
