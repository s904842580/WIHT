package com.waht.platform.audit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.waht.platform.common.security.CurrentUser;
import com.waht.platform.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Runs exclusively against an isolated in-memory database, never the configured MySQL. */
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:audit_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.hikari.minimum-idle=1", "spring.datasource.hikari.maximum-pool-size=4"
})
class AuditPersistenceTests {
    @Autowired JdbcTemplate jdbc;
    @Autowired AuditWriter writer;
    @Autowired AuditMapper mapper;
    @Autowired AuditController controller;
    @Autowired PlatformTransactionManager transactions;
    @Autowired MockMvc mvc;
    private final CurrentUser admin = new CurrentUser(1L, "admin", "ADMIN");

    @BeforeEach void schema() throws Exception {
        try (var connection = jdbc.getDataSource().getConnection()) {
            assertThat(connection.getMetaData().getURL()).startsWith("jdbc:h2:mem:audit_test");
        }
        String migration = Files.readString(Path.of("../../database/mysql/migration/20260918_001_add_audit_log.sql"));
        String ddl = migration.substring(migration.indexOf("CREATE TABLE"));
        jdbc.execute(ddl);
        jdbc.execute("DELETE FROM waht_audit_log");
        jdbc.execute("CREATE TABLE IF NOT EXISTS audit_test_business (id INT PRIMARY KEY)");
        jdbc.execute("DELETE FROM audit_test_business");
    }

    @Test void realMvcConfigurationCapturesDeniedNoteWrites() throws Exception {
        mvc.perform(post("/api/my/notes").header("X-Request-Id", "real-note-denied"))
                .andExpect(status().isUnauthorized());
        AuditEvent saved = mapper.selectOne(new QueryWrapper<AuditEvent>().eq("request_id", "real-note-denied"));
        assertThat(saved.action).isEqualTo("NOTE_CREATE");
        assertThat(saved.outcome).isEqualTo("FAILURE");
        assertThat(saved.userId).isNull();
    }

    @Test void realLoginValidationFailureIsAuditedWithoutPayload() throws Exception {
        mvc.perform(post("/api/auth/login").header("X-Request-Id", "invalid-login")
                .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        AuditEvent saved = mapper.selectOne(new QueryWrapper<AuditEvent>().eq("request_id", "invalid-login"));
        assertThat(saved.action).isEqualTo("LOGIN");
        assertThat(saved.outcome).isEqualTo("FAILURE");
    }

    @Test void auditCommitsIndependentlyOfBusinessRollback() {
        AuditEvent event = event("author", "NOTE_CREATE", "2026-09-18T03:00:00");
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            jdbc.update("INSERT INTO audit_test_business (id) VALUES (1)");
            writer.append(event);
            status.setRollbackOnly();
        });
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_test_business", Integer.class)).isZero();
        AuditEvent saved = mapper.selectById(event.id);
        assertThat(saved.username).isEqualTo("author");
        assertThat(saved.occurredAt).isEqualTo(event.occurredAt);
        assertThat(saved.requestId).isEqualTo(event.requestId);
    }

    @Test void paginationFiltersAndUtcRangeUseActualMapper() {
        writer.append(event("alice", "NOTE_CREATE", "2026-09-18T03:00:00"));
        writer.append(event("alice", "NOTE_CREATE", "2026-09-18T04:00:00"));
        writer.append(event("bob", "NOTE_DELETE", "2026-09-18T04:30:00"));
        var result = controller.list(admin, 1, 1, "alice", "NOTE_CREATE", "SUCCESS", null,
                OffsetDateTime.parse("2026-09-18T10:00:00+08:00"), OffsetDateTime.parse("2026-09-18T12:00:00+08:00")).getData();
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).occurredAt).isEqualTo(LocalDateTime.parse("2026-09-18T04:00:00"));
        var second = controller.list(admin, 2, 1, "alice", "NOTE_CREATE", "SUCCESS", null, null, null).getData();
        assertThat(second.getPage()).isEqualTo(2);
        assertThat(second.getItems().get(0).occurredAt).isEqualTo(LocalDateTime.parse("2026-09-18T03:00:00"));
        var overflow = controller.list(admin, 100, 1, null, null, null, null, null, null).getData();
        assertThat(overflow.getPage()).isEqualTo(1); // Existing pagination configuration resets overflow.
    }

    @Test void filtersAreBoundValuesNotSql() {
        writer.append(event("alice", "NOTE_CREATE", "2026-09-18T03:00:00"));
        var result = controller.list(admin, 1, 20, "' OR 1=1 --", null, null, null, null, null).getData();
        assertThat(result.getTotal()).isZero();
        assertThat(mapper.selectCount(new QueryWrapper<>())).isEqualTo(1);
    }

    @Test void rejectsInvalidBoundsAndUnauthorizedReads() {
        assertThatThrownBy(() -> controller.list(admin, 0, 20, null, null, null, null, null, null)).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> controller.list(admin, 1, 51, null, null, null, null, null, null)).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> controller.list(admin, 1, 20, null, null, "BOGUS", null, null, null)).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> controller.list(admin, 1, 20, null, null, null, null,
                OffsetDateTime.parse("2026-09-19T00:00:00Z"), OffsetDateTime.parse("2026-09-18T00:00:00Z"))).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> controller.list(null, 1, 20, null, null, null, null, null, null)).isInstanceOf(ServiceException.class);
    }

    private AuditEvent event(String username, String action, String occurredAt) {
        AuditEvent event = new AuditEvent();
        event.id = UUID.randomUUID().toString(); event.occurredAt = LocalDateTime.parse(occurredAt);
        event.userId = 7L; event.username = username; event.module = "NOTE"; event.action = action;
        event.outcome = "SUCCESS"; event.httpStatus = 200; event.businessCode = 0;
        event.requestId = UUID.randomUUID().toString(); event.method = "POST";
        event.route = "/api/my/notes"; event.clientIp = "127.0.0.1"; event.durationMs = 4L;
        return event;
    }
}
