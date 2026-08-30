package com.waht.platform.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waht.platform.common.api.PageResponse;
import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.common.exception.ServiceException;
import com.waht.platform.dto.NoteSaveRequest;
import com.waht.platform.dto.PublishedNoteQueryRequest;
import com.waht.platform.entity.NoteCategoryEntity;
import com.waht.platform.entity.NoteEntity;
import com.waht.platform.entity.NoteTagEntity;
import com.waht.platform.entity.NoteTagRelEntity;
import com.waht.platform.mapper.NoteCategoryMapper;
import com.waht.platform.mapper.NoteMapper;
import com.waht.platform.mapper.NoteTagMapper;
import com.waht.platform.mapper.NoteTagRelMapper;
import com.waht.platform.vo.NoteEditorResponse;
import com.waht.platform.vo.NoteSummaryResponse;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * NoteService 写作规则测试，覆盖作者归属、草稿创建和发布内容约束。
 */
@ExtendWith(MockitoExtension.class)
class NoteServiceTests {

    private final NoteMapper noteMapper = mock(NoteMapper.class);
    private final NoteCategoryMapper categoryMapper = mock(NoteCategoryMapper.class);
    private final NoteTagMapper tagMapper = mock(NoteTagMapper.class);
    private final NoteTagRelMapper tagRelMapper = mock(NoteTagRelMapper.class);
    private final NoteService noteService = new NoteService(noteMapper, categoryMapper, tagMapper, tagRelMapper);

    @BeforeAll
    static void initializeMybatisPlusTableMetadata() {
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(new MybatisConfiguration(), "unit-test");
        TableInfoHelper.initTableInfo(builderAssistant, NoteEntity.class);
    }

    @Test
    void listPublishedNotesShouldReturnPageAndApplyFilters() {
        PublishedNoteQueryRequest query = new PublishedNoteQueryRequest();
        query.setPage(2);
        query.setPageSize(5);
        query.setKeyword("Spring");
        query.setCategoryId(2L);
        query.setTagId(5L);

        NoteEntity note = new NoteEntity();
        note.setId(21L);
        note.setTitle("Spring Boot 分页实践");
        note.setSlug("spring-boot-pagination");
        note.setSummary("整理分页查询的实现方式");
        note.setCategoryId(2L);
        note.setStatus("PUBLISHED");
        note.setViewCount(3L);

        NoteCategoryEntity category = activeCategory();
        category.setName("Java");
        category.setSlug("java");

        when(noteMapper.selectPage(
                ArgumentMatchers.<Page<NoteEntity>>any(),
                ArgumentMatchers.<Wrapper<NoteEntity>>any())).thenAnswer(invocation -> {
            Page<NoteEntity> requestedPage = invocation.getArgument(0);
            requestedPage.setRecords(List.of(note));
            requestedPage.setTotal(7L);
            return requestedPage;
        });
        when(categoryMapper.selectBatchIds(any())).thenReturn(List.of(category));
        when(tagRelMapper.selectByNoteIds(List.of(21L))).thenReturn(List.of());

        PageResponse<NoteSummaryResponse> response = noteService.listPublishedNotes(query);

        assertEquals(2L, response.getPage());
        assertEquals(5L, response.getPageSize());
        assertEquals(7L, response.getTotal());
        assertEquals(2L, response.getTotalPages());
        assertEquals("Spring Boot 分页实践", response.getItems().get(0).getTitle());
        assertEquals("Java", response.getItems().get(0).getCategory().getName());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Wrapper<NoteEntity>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(noteMapper).selectPage(ArgumentMatchers.<Page<NoteEntity>>any(), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("title"));
        assertTrue(sqlSegment.contains("category_id"));
        assertTrue(sqlSegment.contains("waht_note_tag_rel"));
    }

    @Test
    void createNoteShouldSaveDraftOwnedByCurrentUser() {
        NoteSaveRequest request = createRequest();
        NoteCategoryEntity category = activeCategory();
        NoteTagEntity tag = activeTag();
        AtomicReference<NoteEntity> insertedNote = new AtomicReference<>();

        when(categoryMapper.selectById(2L)).thenReturn(category);
        when(tagMapper.selectBatchIds(List.of(5L))).thenReturn(List.of(tag));
        when(noteMapper.selectCount(any())).thenReturn(0L);
        doAnswer(invocation -> {
            NoteEntity note = invocation.getArgument(0);
            note.setId(11L);
            note.setCreatedAt(LocalDateTime.now());
            note.setUpdatedAt(LocalDateTime.now());
            insertedNote.set(note);
            return 1;
        }).when(noteMapper).insert(any(NoteEntity.class));
        when(noteMapper.selectOne(any())).thenAnswer(invocation -> insertedNote.get());

        NoteTagRelEntity relation = new NoteTagRelEntity();
        relation.setNoteId(11L);
        relation.setTagId(5L);
        when(tagRelMapper.selectByNoteIds(List.of(11L))).thenReturn(List.of(relation));

        NoteEditorResponse response = noteService.createNote(request, 7L);

        assertEquals("DRAFT", response.getStatus());
        assertEquals(7L, insertedNote.get().getCreatedBy());
        assertEquals(List.of(5L), response.getTagIds());
        verify(tagRelMapper).insertBatch(11L, List.of(5L));
    }

    @Test
    void publishNoteShouldRejectBlankContent() {
        NoteEntity note = new NoteEntity();
        note.setId(9L);
        note.setCreatedBy(7L);
        note.setContent("  ");
        note.setStatus("DRAFT");
        when(noteMapper.selectOne(any())).thenReturn(note);

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> noteService.publishNote(9L, 7L));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), exception.getCode());
    }

    @Test
    void getMyNoteShouldHideOtherUsersNote() {
        when(noteMapper.selectOne(any())).thenReturn(null);

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> noteService.getMyNote(20L, 7L));

        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    private NoteSaveRequest createRequest() {
        NoteSaveRequest request = new NoteSaveRequest();
        request.setTitle("MyBatis-Plus 学习记录");
        request.setSlug("mybatis-plus-study");
        request.setSummary("记录基础 CRUD 和条件构造器。");
        request.setContent("## 今日学习\n\n完成笔记写入链路。");
        request.setCategoryId(2L);
        request.setTagIds(List.of(5L));
        return request;
    }

    private NoteCategoryEntity activeCategory() {
        NoteCategoryEntity category = new NoteCategoryEntity();
        category.setId(2L);
        category.setStatus("ACTIVE");
        return category;
    }

    private NoteTagEntity activeTag() {
        NoteTagEntity tag = new NoteTagEntity();
        tag.setId(5L);
        tag.setStatus("ACTIVE");
        return tag;
    }
}
