package com.waht.platform;

import com.waht.platform.agent.mapper.AgentNoteDraftRequestMapper;
import com.waht.platform.mapper.UserMapper;
import com.waht.platform.mapper.NoteCategoryMapper;
import com.waht.platform.mapper.NoteMapper;
import com.waht.platform.mapper.NoteTagMapper;
import com.waht.platform.mapper.NoteTagRelMapper;
import com.waht.platform.mapper.ProjectLinkMapper;
import com.waht.platform.mapper.ProjectMapper;
import com.waht.platform.mapper.ProjectTechRelMapper;
import com.waht.platform.mapper.TechStackMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
})
class WahtJavaApplicationTests {

    @MockBean
    private com.waht.platform.audit.AuditMapper auditMapper;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private NoteMapper noteMapper;

    @MockBean
    private NoteCategoryMapper noteCategoryMapper;

    @MockBean
    private NoteTagMapper noteTagMapper;

    @MockBean
    private NoteTagRelMapper noteTagRelMapper;

    @MockBean
    private ProjectMapper projectMapper;

    @MockBean
    private ProjectLinkMapper projectLinkMapper;

    @MockBean
    private TechStackMapper techStackMapper;

    @MockBean
    private ProjectTechRelMapper projectTechRelMapper;

    @MockBean
    private AgentNoteDraftRequestMapper agentNoteDraftRequestMapper;

    @Test
    void contextLoads() {
    }
}
