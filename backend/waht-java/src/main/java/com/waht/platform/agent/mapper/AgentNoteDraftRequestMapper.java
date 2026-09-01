package com.waht.platform.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.waht.platform.agent.entity.AgentNoteDraftRequestEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Agent 草稿幂等记录 Mapper，基础 CRUD 直接使用 MyBatis-Plus。
 */
@Mapper
public interface AgentNoteDraftRequestMapper extends BaseMapper<AgentNoteDraftRequestEntity> {
}
