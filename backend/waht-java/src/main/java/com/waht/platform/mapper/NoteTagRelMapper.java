package com.waht.platform.mapper;

import com.waht.platform.entity.NoteTagRelEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
}
