package com.waht.platform.controller;

import com.waht.platform.common.api.ApiResponse;
import com.waht.platform.service.ProjectService;
import com.waht.platform.vo.TechStackResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tech-stacks")
public class TechStackController {

    private final ProjectService projectService;

    public TechStackController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResponse<List<TechStackResponse>> listActiveTechStacks() {
        return ApiResponse.success(projectService.listActiveTechStacks());
    }
}
