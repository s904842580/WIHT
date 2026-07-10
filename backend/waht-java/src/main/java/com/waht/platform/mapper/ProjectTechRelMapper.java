package com.waht.platform.mapper;

import com.waht.platform.entity.ProjectTechRelEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProjectTechRelMapper {

    @Select({
            "<script>",
            "SELECT project_id AS projectId, tech_id AS techId, created_at AS createdAt",
            "FROM waht_project_tech_rel",
            "WHERE project_id IN",
            "<foreach collection='projectIds' item='projectId' open='(' separator=',' close=')'>",
            "#{projectId}",
            "</foreach>",
            "ORDER BY project_id ASC, tech_id ASC",
            "</script>"
    })
    List<ProjectTechRelEntity> selectByProjectIds(@Param("projectIds") List<Long> projectIds);
}
