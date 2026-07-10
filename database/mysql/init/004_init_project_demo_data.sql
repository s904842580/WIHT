-- WAHT project demo data
-- 用途：给项目展示公开查询接口提供本地演示数据。

USE `waht`;

INSERT INTO `waht_tech_stack` (`name`, `slug`, `tech_type`, `icon_url`, `sort_order`, `status`, `created_at`, `updated_at`)
SELECT 'Java', 'java', 'LANGUAGE', NULL, 10, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_tech_stack` WHERE `slug` = 'java');

INSERT INTO `waht_tech_stack` (`name`, `slug`, `tech_type`, `icon_url`, `sort_order`, `status`, `created_at`, `updated_at`)
SELECT 'Spring Boot', 'spring-boot', 'FRAMEWORK', NULL, 20, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_tech_stack` WHERE `slug` = 'spring-boot');

INSERT INTO `waht_tech_stack` (`name`, `slug`, `tech_type`, `icon_url`, `sort_order`, `status`, `created_at`, `updated_at`)
SELECT 'MyBatis-Plus', 'mybatis-plus', 'FRAMEWORK', NULL, 30, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_tech_stack` WHERE `slug` = 'mybatis-plus');

INSERT INTO `waht_tech_stack` (`name`, `slug`, `tech_type`, `icon_url`, `sort_order`, `status`, `created_at`, `updated_at`)
SELECT 'MySQL', 'mysql', 'DATABASE', NULL, 40, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_tech_stack` WHERE `slug` = 'mysql');

INSERT INTO `waht_tech_stack` (`name`, `slug`, `tech_type`, `icon_url`, `sort_order`, `status`, `created_at`, `updated_at`)
SELECT 'React', 'react', 'FRAMEWORK', NULL, 50, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_tech_stack` WHERE `slug` = 'react');

INSERT INTO `waht_tech_stack` (`name`, `slug`, `tech_type`, `icon_url`, `sort_order`, `status`, `created_at`, `updated_at`)
SELECT 'TypeScript', 'typescript', 'LANGUAGE', NULL, 60, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_tech_stack` WHERE `slug` = 'typescript');

INSERT INTO `waht_tech_stack` (`name`, `slug`, `tech_type`, `icon_url`, `sort_order`, `status`, `created_at`, `updated_at`)
SELECT 'Vite', 'vite', 'TOOL', NULL, 70, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_tech_stack` WHERE `slug` = 'vite');

INSERT INTO `waht_tech_stack` (`name`, `slug`, `tech_type`, `icon_url`, `sort_order`, `status`, `created_at`, `updated_at`)
SELECT 'Tailwind CSS', 'tailwind-css', 'FRAMEWORK', NULL, 80, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_tech_stack` WHERE `slug` = 'tailwind-css');

INSERT INTO `waht_project` (
  `name`,
  `slug`,
  `summary`,
  `description`,
  `cover_asset_id`,
  `status`,
  `sort_order`,
  `started_at`,
  `ended_at`,
  `created_at`,
  `updated_at`
)
SELECT
  'WAHT Java 后端',
  'waht-java-backend',
  'Spring Boot 3 主业务后端，当前负责认证、健康检查、学习笔记和项目展示公开接口。',
  '## 项目目标\n\n先用 Java 单体后端承载 WAHT 的核心业务，保证登录、内容查询和数据库访问链路稳定。\n\n## 已完成\n\n- 统一响应\n- 全局异常\n- JWT 鉴权\n- 学习笔记公开查询\n- 项目展示公开查询\n\n## 下一步\n\n补充后台管理接口，让管理员可以维护笔记和项目内容。',
  NULL,
  'PUBLISHED',
  10,
  '2026-06-26',
  NULL,
  NOW(),
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_project` WHERE `slug` = 'waht-java-backend');

INSERT INTO `waht_project` (
  `name`,
  `slug`,
  `summary`,
  `description`,
  `cover_asset_id`,
  `status`,
  `sort_order`,
  `started_at`,
  `ended_at`,
  `created_at`,
  `updated_at`
)
SELECT
  'WAHT Web 前台',
  'waht-web-frontend',
  'React + TypeScript + Vite 前台页面，当前负责首页、登录注册、学习笔记和项目展示。',
  '## 项目目标\n\n给 WAHT 提供用户可见的前台入口，把后端接口变成可浏览、可操作的页面。\n\n## 已完成\n\n- React Router 页面路由\n- TanStack Query 接口请求\n- 登录注册页面\n- 学习笔记列表和详情\n- 项目列表和详情\n\n## 下一步\n\n继续补素材展示，并把更多静态页面替换成后端数据。',
  NULL,
  'PUBLISHED',
  20,
  '2026-06-28',
  NULL,
  NOW(),
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM `waht_project` WHERE `slug` = 'waht-web-frontend');

INSERT INTO `waht_project_link` (`project_id`, `link_type`, `title`, `url`, `sort_order`, `created_at`, `updated_at`)
SELECT p.`id`, 'DOC', '后端说明', '/notes/spring-boot-auth-flow', 10, NOW(), NOW()
FROM `waht_project` p
WHERE p.`slug` = 'waht-java-backend'
  AND NOT EXISTS (
    SELECT 1 FROM `waht_project_link` l WHERE l.`project_id` = p.`id` AND l.`title` = '后端说明'
  );

INSERT INTO `waht_project_link` (`project_id`, `link_type`, `title`, `url`, `sort_order`, `created_at`, `updated_at`)
SELECT p.`id`, 'DEMO', '健康检查', '/api/health', 20, NOW(), NOW()
FROM `waht_project` p
WHERE p.`slug` = 'waht-java-backend'
  AND NOT EXISTS (
    SELECT 1 FROM `waht_project_link` l WHERE l.`project_id` = p.`id` AND l.`title` = '健康检查'
  );

INSERT INTO `waht_project_link` (`project_id`, `link_type`, `title`, `url`, `sort_order`, `created_at`, `updated_at`)
SELECT p.`id`, 'DEMO', '前台首页', '/', 10, NOW(), NOW()
FROM `waht_project` p
WHERE p.`slug` = 'waht-web-frontend'
  AND NOT EXISTS (
    SELECT 1 FROM `waht_project_link` l WHERE l.`project_id` = p.`id` AND l.`title` = '前台首页'
  );

INSERT INTO `waht_project_link` (`project_id`, `link_type`, `title`, `url`, `sort_order`, `created_at`, `updated_at`)
SELECT p.`id`, 'DOC', 'React 工程笔记', '/notes/react-web-starter', 20, NOW(), NOW()
FROM `waht_project` p
WHERE p.`slug` = 'waht-web-frontend'
  AND NOT EXISTS (
    SELECT 1 FROM `waht_project_link` l WHERE l.`project_id` = p.`id` AND l.`title` = 'React 工程笔记'
  );

INSERT INTO `waht_project_tech_rel` (`project_id`, `tech_id`, `created_at`)
SELECT p.`id`, t.`id`, NOW()
FROM `waht_project` p
JOIN `waht_tech_stack` t ON t.`slug` IN ('java', 'spring-boot', 'mybatis-plus', 'mysql')
WHERE p.`slug` = 'waht-java-backend'
  AND NOT EXISTS (
    SELECT 1 FROM `waht_project_tech_rel` r WHERE r.`project_id` = p.`id` AND r.`tech_id` = t.`id`
  );

INSERT INTO `waht_project_tech_rel` (`project_id`, `tech_id`, `created_at`)
SELECT p.`id`, t.`id`, NOW()
FROM `waht_project` p
JOIN `waht_tech_stack` t ON t.`slug` IN ('react', 'typescript', 'vite', 'tailwind-css')
WHERE p.`slug` = 'waht-web-frontend'
  AND NOT EXISTS (
    SELECT 1 FROM `waht_project_tech_rel` r WHERE r.`project_id` = p.`id` AND r.`tech_id` = t.`id`
  );
