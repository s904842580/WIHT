package com.waht.platform.controller;

import com.waht.platform.common.api.BaseResponse;
import com.waht.platform.service.ProjectService;
import com.waht.platform.vo.ProjectDetailResponse;
import com.waht.platform.vo.ProjectSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目展示模块的公开查询接口。
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public BaseResponse<List<ProjectSummaryResponse>> listPublishedProjects() {
        return BaseResponse.success(projectService.listPublishedProjects());
    }

    @GetMapping("/{slug}")
    public BaseResponse<ProjectDetailResponse> getPublishedProject(@PathVariable String slug) {
        return BaseResponse.success(projectService.getPublishedProjectBySlug(slug));
    }
}
