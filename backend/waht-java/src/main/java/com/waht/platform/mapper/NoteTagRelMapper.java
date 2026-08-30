package com.waht.platform.mapper;

import com.waht.platform.entity.NoteTagRelEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 笔记与标签的复合主键关系 Mapper，显式 SQL 避免 BaseMapper 误判单一主键。
 */
@Mapper
public interface NoteTagRelMapper {

    @Select({
            "<script>",
            "SELECT note_id AS noteId, tag_id AS tagId, created_at AS createdAt",
            "FROM waht_note_tag_rel",
            "WHERE note_id IN",
            "<foreach collection='noteIds' item='noteId' open='(' separator=',' close=')'>",
            "#{noteId}",
            "</foreach>",
            "ORDER BY note_id ASC, tag_id ASC",
            "</script>"
    })
    List<NoteTagRelEntity> selectByNoteIds(@Param("noteIds") List<Long> noteIds);

    @Delete("DELETE FROM waht_note_tag_rel WHERE note_id = #{noteId}")
    int deleteByNoteId(@Param("noteId") Long noteId);

    @Insert({
            "<script>",
            "INSERT INTO waht_note_tag_rel (note_id, tag_id, created_at) VALUES",
            "<foreach collection='tagIds' item='tagId' separator=','>",
            "(#{noteId}, #{tagId}, NOW())",
            "</foreach>",
            "</script>"
    })
    int insertBatch(@Param("noteId") Long noteId, @Param("tagIds") List<Long> tagIds);
}
