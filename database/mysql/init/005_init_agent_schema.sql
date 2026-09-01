-- WAHT Python Agent 独立数据库。
-- Agent 只保存会话、运行、工具摘要和审批，不复制 Java 的用户与笔记业务表。

CREATE DATABASE IF NOT EXISTS `waht_ai`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `waht_ai`;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `agent_conversation` (
  `id` VARCHAR(36) NOT NULL COMMENT 'UUID 主键',
  `user_id` BIGINT NOT NULL COMMENT 'Java 用户 ID，仅作为业务归属标识',
  `title` VARCHAR(120) NOT NULL COMMENT '会话标题',
  `status` VARCHAR(24) NOT NULL DEFAULT 'ACTIVE' COMMENT '会话状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_agent_conversation_user_updated` (`user_id`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 会话';

CREATE TABLE IF NOT EXISTS `agent_message` (
  `id` VARCHAR(36) NOT NULL COMMENT 'UUID 主键',
  `conversation_id` VARCHAR(36) NOT NULL COMMENT '会话 ID',
  `role` VARCHAR(16) NOT NULL COMMENT 'USER 或 ASSISTANT',
  `content` LONGTEXT NOT NULL COMMENT '消息正文',
  `sources` JSON NOT NULL COMMENT '引用笔记的最小摘要',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_agent_message_conversation_created` (`conversation_id`, `created_at`),
  CONSTRAINT `fk_agent_message_conversation`
    FOREIGN KEY (`conversation_id`) REFERENCES `agent_conversation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 消息';

CREATE TABLE IF NOT EXISTS `agent_run` (
  `id` VARCHAR(64) NOT NULL COMMENT 'Java 生成的单次运行 ID',
  `conversation_id` VARCHAR(36) NOT NULL COMMENT '会话 ID',
  `user_id` BIGINT NOT NULL COMMENT '运行所属 Java 用户 ID',
  `status` VARCHAR(32) NOT NULL COMMENT '运行状态',
  `model` VARCHAR(100) NOT NULL COMMENT '模型名称',
  `input_tokens` INT NOT NULL DEFAULT 0 COMMENT '输入 token 数',
  `output_tokens` INT NOT NULL DEFAULT 0 COMMENT '输出 token 数',
  `total_tokens` INT NOT NULL DEFAULT 0 COMMENT '总 token 数',
  `error_message` VARCHAR(1000) DEFAULT NULL COMMENT '失败摘要，不记录授权令牌',
  `started_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `finished_at` DATETIME DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  KEY `idx_agent_run_conversation` (`conversation_id`),
  KEY `idx_agent_run_user_started` (`user_id`, `started_at`),
  CONSTRAINT `fk_agent_run_conversation`
    FOREIGN KEY (`conversation_id`) REFERENCES `agent_conversation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 单次运行';

CREATE TABLE IF NOT EXISTS `agent_tool_call` (
  `id` VARCHAR(36) NOT NULL COMMENT 'UUID 主键',
  `run_id` VARCHAR(64) NOT NULL COMMENT '运行 ID',
  `tool_name` VARCHAR(100) NOT NULL COMMENT '工具名称',
  `arguments` JSON NOT NULL COMMENT '脱敏后的工具参数',
  `result_summary` JSON NOT NULL COMMENT '工具结果摘要，不保存完整令牌',
  `status` VARCHAR(24) NOT NULL COMMENT '工具状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_agent_tool_call_run` (`run_id`),
  CONSTRAINT `fk_agent_tool_call_run`
    FOREIGN KEY (`run_id`) REFERENCES `agent_run` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 工具调用摘要';

CREATE TABLE IF NOT EXISTS `agent_approval` (
  `id` VARCHAR(36) NOT NULL COMMENT 'UUID 主键，同时作为 Java 写入幂等键',
  `run_id` VARCHAR(64) NOT NULL COMMENT '运行 ID',
  `action` VARCHAR(100) NOT NULL COMMENT '待审批动作',
  `status` VARCHAR(24) NOT NULL COMMENT '审批状态',
  `payload` JSON NOT NULL COMMENT '原始草稿',
  `decided_payload` JSON DEFAULT NULL COMMENT '用户修改后的最终草稿',
  `created_note_id` BIGINT DEFAULT NULL COMMENT 'Java 创建成功后的笔记 ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `decided_at` DATETIME DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`),
  KEY `idx_agent_approval_run_status` (`run_id`, `status`),
  CONSTRAINT `fk_agent_approval_run`
    FOREIGN KEY (`run_id`) REFERENCES `agent_run` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 高风险动作审批';
