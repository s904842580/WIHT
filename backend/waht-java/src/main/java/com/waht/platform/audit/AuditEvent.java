package com.waht.platform.audit;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/** Append-only metadata. UTC timestamps; no bodies, tokens or arbitrary error text. */
@TableName("waht_audit_log")
public class AuditEvent {
    @TableId(type = IdType.INPUT)
    public String id;
    public LocalDateTime occurredAt;
    public Long userId;
    public String username;
    public String module;
    public String action;
    public String resourceId;
    public String outcome;
    public String operationState;
    public Integer httpStatus;
    public Integer businessCode;
    public String requestId;
    public String method;
    public String route;
    public String clientIp;
    public Long durationMs;
}
