-- WAHT note demo data
-- 用途：给学习笔记公开查询接口提供本地演示数据。

USE `waht`;

INSERT INTO `waht_note_category` (`name`, `slug`, `description`, `sort_order`, `status`, `created_at`, `updated_at`)
SELECT 'Java 后端', 'java-backend', 'Spring Boot、MyBatis-Plus、认证和接口设计记录。', 10, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_note_category` WHERE `slug` = 'java-backend');

INSERT INTO `waht_note_category` (`name`, `slug`, `description`, `sort_order`, `status`, `created_at`, `updated_at`)
SELECT '数据库', 'database', 'MySQL 表结构、索引和初始化脚本记录。', 20, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_note_category` WHERE `slug` = 'database');

INSERT INTO `waht_note_category` (`name`, `slug`, `description`, `sort_order`, `status`, `created_at`, `updated_at`)
SELECT '前端工程', 'frontend', 'React、TypeScript、Vite 和接口联调记录。', 30, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_note_category` WHERE `slug` = 'frontend');

INSERT INTO `waht_note_tag` (`name`, `slug`, `color`, `status`, `created_at`, `updated_at`)
SELECT 'Spring Boot', 'spring-boot', '#277568', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_note_tag` WHERE `slug` = 'spring-boot');

INSERT INTO `waht_note_tag` (`name`, `slug`, `color`, `status`, `created_at`, `updated_at`)
SELECT 'MySQL', 'mysql', '#c58b2b', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_note_tag` WHERE `slug` = 'mysql');

INSERT INTO `waht_note_tag` (`name`, `slug`, `color`, `status`, `created_at`, `updated_at`)
SELECT 'React', 'react', '#b33b2e', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_note_tag` WHERE `slug` = 'react');

INSERT INTO `waht_note_tag` (`name`, `slug`, `color`, `status`, `created_at`, `updated_at`)
SELECT 'JWT', 'jwt', '#1f2933', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_note_tag` WHERE `slug` = 'jwt');

INSERT INTO `waht_note` (
  `title`,
  `slug`,
  `summary`,
  `content`,
  `category_id`,
  `cover_asset_id`,
  `status`,
  `view_count`,
  `published_at`,
  `created_by`,
  `updated_by`,
  `created_at`,
  `updated_at`
)
SELECT
  'Spring Boot 登录注册闭环',
  'spring-boot-auth-flow',
  '记录 WAHT 后端从健康检查到登录、注册、JWT 鉴权的第一条业务闭环。',
  '## 目标\n\n把用户登录注册链路跑通。\n\n## 已完成\n\n- 统一响应\n- 全局异常\n- BCrypt 密码加密\n- JWT 生成和解析\n- 当前用户接口\n\n## 下一步\n\n继续补学习笔记公开查询接口。',
  (SELECT `id` FROM `waht_note_category` WHERE `slug` = 'java-backend'),
  NULL,
  'PUBLISHED',
  0,
  NOW(),
  (SELECT `id` FROM `waht_user` WHERE `username` = 'admin'),
  (SELECT `id` FROM `waht_user` WHERE `username` = 'admin'),
  NOW(),
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_note` WHERE `slug` = 'spring-boot-auth-flow');

INSERT INTO `waht_note` (
  `title`,
  `slug`,
  `summary`,
  `content`,
  `category_id`,
  `cover_asset_id`,
  `status`,
  `view_count`,
  `published_at`,
  `created_by`,
  `updated_by`,
  `created_at`,
  `updated_at`
)
SELECT
  'MySQL 初始化脚本设计',
  'mysql-init-schema-design',
  '记录 WAHT 第一阶段 MySQL 表结构、初始化 SQL 和演示数据的组织方式。',
  '## 目标\n\n先用清晰的表结构承载用户、笔记、项目和素材元数据。\n\n## 当前策略\n\n- 初始建表脚本放在 init 目录\n- 后续变更放在 migration 目录\n- 第一阶段先用逻辑外键\n\n## 注意\n\n已执行过的 SQL 不要随意修改。',
  (SELECT `id` FROM `waht_note_category` WHERE `slug` = 'database'),
  NULL,
  'PUBLISHED',
  0,
  DATE_SUB(NOW(), INTERVAL 1 DAY),
  (SELECT `id` FROM `waht_user` WHERE `username` = 'admin'),
  (SELECT `id` FROM `waht_user` WHERE `username` = 'admin'),
  NOW(),
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_note` WHERE `slug` = 'mysql-init-schema-design');

INSERT INTO `waht_note` (
  `title`,
  `slug`,
  `summary`,
  `content`,
  `category_id`,
  `cover_asset_id`,
  `status`,
  `view_count`,
  `published_at`,
  `created_by`,
  `updated_by`,
  `created_at`,
  `updated_at`
)
SELECT
  'React 前台工程搭建',
  'react-web-starter',
  '记录 WAHT 前台使用 React、TypeScript、Vite 和 Tailwind CSS 搭建的基础结构。',
  '## 目标\n\n让前端可以访问后端登录、注册和公开内容接口。\n\n## 已完成\n\n- Vite React 工程\n- React Router\n- TanStack Query\n- Tailwind CSS\n- 登录页和注册页\n\n## 下一步\n\n把学习笔记静态数据替换为后端接口数据。',
  (SELECT `id` FROM `waht_note_category` WHERE `slug` = 'frontend'),
  NULL,
  'PUBLISHED',
  0,
  DATE_SUB(NOW(), INTERVAL 2 DAY),
  (SELECT `id` FROM `waht_user` WHERE `username` = 'admin'),
  (SELECT `id` FROM `waht_user` WHERE `username` = 'admin'),
  NOW(),
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_note` WHERE `slug` = 'react-web-starter');

INSERT INTO `waht_note_tag_rel` (`note_id`, `tag_id`, `created_at`)
SELECT n.`id`, t.`id`, NOW()
FROM `waht_note` n
JOIN `waht_note_tag` t ON t.`slug` IN ('spring-boot', 'jwt')
WHERE n.`slug` = 'spring-boot-auth-flow'
  AND NOT EXISTS (
    SELECT 1 FROM `waht_note_tag_rel` r WHERE r.`note_id` = n.`id` AND r.`tag_id` = t.`id`
  );

INSERT INTO `waht_note_tag_rel` (`note_id`, `tag_id`, `created_at`)
SELECT n.`id`, t.`id`, NOW()
FROM `waht_note` n
JOIN `waht_note_tag` t ON t.`slug` = 'mysql'
WHERE n.`slug` = 'mysql-init-schema-design'
  AND NOT EXISTS (
    SELECT 1 FROM `waht_note_tag_rel` r WHERE r.`note_id` = n.`id` AND r.`tag_id` = t.`id`
  );

INSERT INTO `waht_note_tag_rel` (`note_id`, `tag_id`, `created_at`)
SELECT n.`id`, t.`id`, NOW()
FROM `waht_note` n
JOIN `waht_note_tag` t ON t.`slug` = 'react'
WHERE n.`slug` = 'react-web-starter'
  AND NOT EXISTS (
    SELECT 1 FROM `waht_note_tag_rel` r WHERE r.`note_id` = n.`id` AND r.`tag_id` = t.`id`
  );
