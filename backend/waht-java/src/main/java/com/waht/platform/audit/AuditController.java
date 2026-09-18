package com.waht.platform.audit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waht.platform.common.api.BaseResponse;
import com.waht.platform.common.api.PageResponse;
import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.common.exception.ServiceException;
import com.waht.platform.common.security.CurrentUser;
import com.waht.platform.common.security.LoginUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditController {
    private final AuditMapper mapper;

    public AuditController(AuditMapper mapper) { this.mapper = mapper; }

    @GetMapping
    @Audited(module = "AUDIT", action = "QUERY")
    public BaseResponse<PageResponse<AuditEvent>> list(
            @LoginUser CurrentUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        if (user == null) throw new ServiceException(ErrorCode.UNAUTHORIZED, "请先登录");
        if (!"ADMIN".equals(user.role())) throw new ServiceException(ErrorCode.FORBIDDEN, "仅管理员可查询操作审计");
        if (page < 1 || page > 100000 || pageSize < 1 || pageSize > 50
                || (from != null && to != null && from.isAfter(to))) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "分页或时间范围不合法");
        }
        validateLength(username, 50);
        validateLength(action, 40);
        validateLength(requestId, 64);
        if (outcome != null && !Set.of("SUCCESS", "FAILURE", "UNKNOWN").contains(outcome)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "操作结果不合法");
        }
        QueryWrapper<AuditEvent> query = new QueryWrapper<>();
        query.eq(username != null, "username", username)
                .eq(action != null, "action", action)
                .eq(outcome != null, "outcome", outcome)
                .eq(requestId != null, "request_id", requestId);
        if (from != null) query.ge("occurred_at", from.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
        if (to != null) query.le("occurred_at", to.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
        query.orderByDesc("occurred_at", "id");
        Page<AuditEvent> result = mapper.selectPage(new Page<>(page, pageSize), query);
        return BaseResponse.success(PageResponse.of(result.getRecords(), result.getCurrent(), result.getSize(), result.getTotal()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<Void>> invalidParameter() {
        return ResponseEntity.badRequest().body(BaseResponse.fail(ErrorCode.BAD_REQUEST.getCode(), "分页或时间参数格式不合法"));
    }

    private void validateLength(String value, int max) {
        if (value != null && (value.isBlank() || value.length() > max)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "筛选字段长度不合法");
        }
    }
}
