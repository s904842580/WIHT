package com.waht.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.waht.platform.entity.NoteEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 笔记表 MyBatis-Plus Mapper；简单 CRUD 使用 BaseMapper，自增浏览量使用原子 SQL。
 */
@Mapper
public interface NoteMapper extends BaseMapper<NoteEntity> {

    @Update("UPDATE waht_note SET view_count = view_count + 1, updated_at = updated_at WHERE id = #{noteId}")
    int incrementViewCount(@Param("noteId") Long noteId);
}
