-- Agent 审批写入幂等表。
-- 同一个 approvalId 只能落一篇草稿，避免跨服务超时重试造成重复笔记。

USE `waht`;

CREATE TABLE IF NOT EXISTS `waht_agent_note_draft_request` (
  `idempotency_key` VARCHAR(64) NOT NULL COMMENT 'Python approvalId，幂等键',
  `user_id` BIGINT NOT NULL COMMENT '草稿所属用户 ID',
  `note_id` BIGINT NOT NULL COMMENT '已创建的笔记 ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`idempotency_key`),
  KEY `idx_agent_note_draft_user_id` (`user_id`),
  KEY `idx_agent_note_draft_note_id` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 草稿审批幂等记录';
