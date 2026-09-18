-- 新增独立审计表；适用于已执行 001_init_schema.sql 的新旧安装。
-- 不修改历史业务数据。occurred_at 使用 UTC，由应用显式写入。
USE `waht`;

CREATE TABLE IF NOT EXISTS `waht_audit_log` (
  `id` VARCHAR(36) NOT NULL COMMENT '服务端审计事件 UUID',
  `occurred_at` DATETIME(6) NOT NULL COMMENT '请求开始时间 UTC',
  `user_id` BIGINT DEFAULT NULL COMMENT '已验证用户，认证失败时为空',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '已验证用户名快照',
  `module` VARCHAR(24) NOT NULL,
  `action` VARCHAR(40) NOT NULL,
  `resource_id` VARCHAR(64) DEFAULT NULL,
  `outcome` VARCHAR(16) NOT NULL COMMENT 'SUCCESS/FAILURE/UNKNOWN 接口结果',
  `operation_state` VARCHAR(32) DEFAULT NULL COMMENT '白名单业务状态',
  `http_status` INT NOT NULL,
  `business_code` INT DEFAULT NULL,
  `request_id` VARCHAR(64) NOT NULL,
  `method` VARCHAR(10) NOT NULL,
  `route` VARCHAR(200) NOT NULL COMMENT '路由模板，不含查询字符串',
  `client_ip` VARCHAR(64) DEFAULT NULL COMMENT '直接连接地址',
  `duration_ms` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_audit_time` (`occurred_at`, `id`),
  KEY `idx_audit_user_time` (`username`, `occurred_at`),
  KEY `idx_audit_action_time` (`action`, `occurred_at`),
  KEY `idx_audit_request` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用操作审计，仅追加';
