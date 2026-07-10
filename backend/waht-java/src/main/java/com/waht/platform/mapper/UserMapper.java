package com.waht.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.waht.platform.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
