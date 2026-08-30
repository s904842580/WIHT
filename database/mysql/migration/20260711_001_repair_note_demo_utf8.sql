-- 修复早期本地导入时被错误客户端字符集写成问号的笔记演示数据。
-- 仅按固定 slug 更新项目自带的演示记录，不会修改用户后续创建的笔记。

USE `waht`;

SET NAMES utf8mb4;

UPDATE `waht_note_category`
SET `name` = 'Java 后端',
    `description` = 'Spring Boot、MyBatis-Plus、认证和接口设计记录。'
WHERE `slug` = 'java-backend';

UPDATE `waht_note_category`
SET `name` = '数据库',
    `description` = 'MySQL 表结构、索引和初始化脚本记录。'
WHERE `slug` = 'database';

UPDATE `waht_note_category`
SET `name` = '前端工程',
    `description` = 'React、TypeScript、Vite 和接口联调记录。'
WHERE `slug` = 'frontend';

UPDATE `waht_note_tag` SET `name` = 'Spring Boot' WHERE `slug` = 'spring-boot';
UPDATE `waht_note_tag` SET `name` = 'MySQL' WHERE `slug` = 'mysql';
UPDATE `waht_note_tag` SET `name` = 'React' WHERE `slug` = 'react';
UPDATE `waht_note_tag` SET `name` = 'JWT' WHERE `slug` = 'jwt';

UPDATE `waht_note`
SET `title` = 'Spring Boot 登录注册闭环',
    `summary` = '记录 WAHT 后端从健康检查到登录、注册、JWT 鉴权的第一条业务闭环。',
    `content` = '## 目标\n\n把用户登录注册链路跑通。\n\n## 已完成\n\n- 统一响应\n- 全局异常\n- BCrypt 密码加密\n- JWT 生成和解析\n- 当前用户接口\n\n## 下一步\n\n继续补学习笔记公开查询接口。'
WHERE `slug` = 'spring-boot-auth-flow';

UPDATE `waht_note`
SET `title` = 'MySQL 初始化脚本设计',
    `summary` = '记录 WAHT 第一阶段 MySQL 表结构、初始化 SQL 和演示数据的组织方式。',
    `content` = '## 目标\n\n先用清晰的表结构承载用户、笔记、项目和素材元数据。\n\n## 当前策略\n\n- 初始建表脚本放在 init 目录\n- 后续变更放在 migration 目录\n- 第一阶段先用逻辑外键\n\n## 注意\n\n已执行过的 SQL 不要随意修改。'
WHERE `slug` = 'mysql-init-schema-design';

UPDATE `waht_note`
SET `title` = 'React 前台工程搭建',
    `summary` = '记录 WAHT 前台使用 React、TypeScript、Vite 和 Tailwind CSS 搭建的基础结构。',
    `content` = '## 目标\n\n让前端可以访问后端登录、注册和公开内容接口。\n\n## 已完成\n\n- Vite React 工程\n- React Router\n- TanStack Query\n- Tailwind CSS\n- 登录页和注册页\n\n## 下一步\n\n把学习笔记静态数据替换为后端接口数据。'
WHERE `slug` = 'react-web-starter';
