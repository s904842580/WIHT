package com.waht.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.common.exception.ServiceException;
import com.waht.platform.entity.ProjectEntity;
import com.waht.platform.entity.ProjectLinkEntity;
import com.waht.platform.entity.ProjectTechRelEntity;
import com.waht.platform.entity.TechStackEntity;
import com.waht.platform.mapper.ProjectLinkMapper;
import com.waht.platform.mapper.ProjectMapper;
import com.waht.platform.mapper.ProjectTechRelMapper;
import com.waht.platform.mapper.TechStackMapper;
import com.waht.platform.vo.ProjectDetailResponse;
import com.waht.platform.vo.ProjectLinkResponse;
import com.waht.platform.vo.ProjectSummaryResponse;
import com.waht.platform.vo.TechStackResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 项目公开查询服务，负责聚合项目、链接和技术栈数据。
 */
@Service
public class ProjectService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String PUBLISHED_STATUS = "PUBLISHED";

    private final ProjectMapper projectMapper;
    private final ProjectLinkMapper projectLinkMapper;
    private final TechStackMapper techStackMapper;
    private final ProjectTechRelMapper projectTechRelMapper;

    public ProjectService(
            ProjectMapper projectMapper,
            ProjectLinkMapper projectLinkMapper,
            TechStackMapper techStackMapper,
            ProjectTechRelMapper projectTechRelMapper) {
        this.projectMapper = projectMapper;
        this.projectLinkMapper = projectLinkMapper;
        this.techStackMapper = techStackMapper;
        this.projectTechRelMapper = projectTechRelMapper;
    }

    public List<ProjectSummaryResponse> listPublishedProjects() {
        List<ProjectEntity> projects = projectMapper.selectList(new LambdaQueryWrapper<ProjectEntity>()
                .eq(ProjectEntity::getStatus, PUBLISHED_STATUS)
                .orderByAsc(ProjectEntity::getSortOrder)
                .orderByDesc(ProjectEntity::getStartedAt)
                .orderByDesc(ProjectEntity::getId));
        if (projects.isEmpty()) {
            return List.of();
        }

        Map<Long, List<TechStackResponse>> projectTechMap = loadProjectTechMap(projects);
        Map<Long, List<ProjectLinkResponse>> projectLinkMap = loadProjectLinkMap(projects);

        return projects.stream()
                .map(project -> ProjectSummaryResponse.from(
                        project,
                        projectTechMap.getOrDefault(project.getId(), List.of()),
                        projectLinkMap.getOrDefault(project.getId(), List.of())))
                .toList();
    }

    public ProjectDetailResponse getPublishedProjectBySlug(String slug) {
        ProjectEntity project = projectMapper.selectOne(new LambdaQueryWrapper<ProjectEntity>()
                .eq(ProjectEntity::getSlug, slug)
                .eq(ProjectEntity::getStatus, PUBLISHED_STATUS)
                .last("LIMIT 1"));
        if (project == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "项目不存在");
        }

        return ProjectDetailResponse.from(
                project,
                loadProjectTechMap(List.of(project)).getOrDefault(project.getId(), List.of()),
                loadProjectLinkMap(List.of(project)).getOrDefault(project.getId(), List.of()));
    }

    public List<TechStackResponse> listActiveTechStacks() {
        return techStackMapper.selectList(new LambdaQueryWrapper<TechStackEntity>()
                        .eq(TechStackEntity::getStatus, ACTIVE_STATUS)
                        .orderByAsc(TechStackEntity::getSortOrder)
                        .orderByAsc(TechStackEntity::getId))
                .stream()
                .map(TechStackResponse::from)
                .toList();
    }

    private Map<Long, List<ProjectLinkResponse>> loadProjectLinkMap(List<ProjectEntity> projects) {
        List<Long> projectIds = projects.stream().map(ProjectEntity::getId).toList();
        if (projectIds.isEmpty()) {
            return Map.of();
        }

        List<ProjectLinkEntity> links = projectLinkMapper.selectList(new LambdaQueryWrapper<ProjectLinkEntity>()
                .in(ProjectLinkEntity::getProjectId, projectIds)
                .orderByAsc(ProjectLinkEntity::getProjectId)
                .orderByAsc(ProjectLinkEntity::getSortOrder)
                .orderByAsc(ProjectLinkEntity::getId));
        if (links.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<ProjectLinkResponse>> result = new LinkedHashMap<>();
        for (ProjectLinkEntity link : links) {
            result.computeIfAbsent(link.getProjectId(), key -> new ArrayList<>())
                    .add(ProjectLinkResponse.from(link));
        }

        Map<Long, List<ProjectLinkResponse>> readonlyResult = new HashMap<>();
        result.forEach((projectId, projectLinks) -> readonlyResult.put(projectId, Collections.unmodifiableList(projectLinks)));
        return readonlyResult;
    }

    private Map<Long, List<TechStackResponse>> loadProjectTechMap(List<ProjectEntity> projects) {
        List<Long> projectIds = projects.stream().map(ProjectEntity::getId).toList();
        if (projectIds.isEmpty()) {
            return Map.of();
        }

        List<ProjectTechRelEntity> relations = projectTechRelMapper.selectByProjectIds(projectIds);
        if (relations.isEmpty()) {
            return Map.of();
        }

        Set<Long> techIds = relations.stream()
                .map(ProjectTechRelEntity::getTechId)
                .collect(Collectors.toSet());
        Map<Long, TechStackResponse> techMap = techStackMapper.selectBatchIds(techIds).stream()
                .filter(techStack -> ACTIVE_STATUS.equals(techStack.getStatus()))
                .collect(Collectors.toMap(
                        TechStackEntity::getId,
                        TechStackResponse::from,
                        (oldValue, newValue) -> oldValue));

        Map<Long, List<TechStackResponse>> result = new LinkedHashMap<>();
        for (ProjectTechRelEntity relation : relations) {
            TechStackResponse techStack = techMap.get(relation.getTechId());
            if (techStack == null) {
                continue;
            }
            result.computeIfAbsent(relation.getProjectId(), key -> new ArrayList<>()).add(techStack);
        }

        Map<Long, List<TechStackResponse>> readonlyResult = new HashMap<>();
        result.forEach((projectId, techStacks) -> readonlyResult.put(projectId, Collections.unmodifiableList(techStacks)));
        return readonlyResult;
    }
}
