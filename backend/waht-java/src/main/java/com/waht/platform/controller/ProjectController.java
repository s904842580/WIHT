package com.waht.platform.controller;

import com.waht.platform.common.api.ApiResponse;
import com.waht.platform.service.ProjectService;
import com.waht.platform.vo.ProjectDetailResponse;
import com.waht.platform.vo.ProjectSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResponse<List<ProjectSummaryResponse>> listPublishedProjects() {
        return ApiResponse.success(projectService.listPublishedProjects());
    }

    @GetMapping("/{slug}")
    public ApiResponse<ProjectDetailResponse> getPublishedProject(@PathVariable String slug) {
        return ApiResponse.success(projectService.getPublishedProjectBySlug(slug));
    }
}
