package com.waht.platform.controller;

import com.waht.platform.common.api.BaseResponse;
import com.waht.platform.service.ProjectService;
import com.waht.platform.vo.TechStackResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目展示模块使用的有效技术栈查询接口。
 */
@RestController
@RequestMapping("/api/tech-stacks")
public class TechStackController {

    private final ProjectService projectService;

    public TechStackController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public BaseResponse<List<TechStackResponse>> listActiveTechStacks() {
        return BaseResponse.success(projectService.listActiveTechStacks());
    }
}
