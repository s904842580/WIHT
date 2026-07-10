-- WAHT MySQL init schema
-- 当前范围：第一阶段基础表结构
-- 说明：第一阶段按逻辑外键处理，不创建 FOREIGN KEY 约束。

CREATE DATABASE IF NOT EXISTS `waht`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `waht`;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `waht_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名，唯一',
  `password` VARCHAR(100) NOT NULL COMMENT '密码，建议存储哈希值',
  `nickname` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '昵称',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN、USER',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE、DISABLED',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_waht_user_username` (`username`),
  KEY `idx_waht_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `waht_note_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `slug` VARCHAR(80) NOT NULL COMMENT '分类访问标识，唯一',
  `description` VARCHAR(300) DEFAULT NULL COMMENT '分类说明',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE、HIDDEN',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_waht_note_category_slug` (`slug`),
  KEY `idx_waht_note_category_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记分类表';

CREATE TABLE IF NOT EXISTS `waht_note_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `slug` VARCHAR(80) NOT NULL COMMENT '标签访问标识，唯一',
  `color` VARCHAR(20) DEFAULT NULL COMMENT '标签颜色',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE、HIDDEN',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_waht_note_tag_slug` (`slug`),
  KEY `idx_waht_note_tag_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记标签表';

CREATE TABLE IF NOT EXISTS `waht_asset` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(120) NOT NULL COMMENT '素材名称',
  `asset_type` VARCHAR(30) NOT NULL COMMENT '类型：IMAGE、MODEL、AUDIO、VIDEO、OTHER',
  `storage_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
  `thumbnail_path` VARCHAR(500) DEFAULT NULL COMMENT '缩略图路径',
  `file_ext` VARCHAR(20) DEFAULT NULL COMMENT '文件扩展名',
  `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小，单位 byte',
  `source_type` VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN' COMMENT '来源：ORIGINAL、OFFICIAL、NETWORK、OPEN_SOURCE、UNKNOWN',
  `source_url` VARCHAR(500) DEFAULT NULL COMMENT '来源地址',
  `source_note` VARCHAR(500) DEFAULT NULL COMMENT '来源说明',
  `license_note` VARCHAR(500) DEFAULT NULL COMMENT '授权说明',
  `public_status` VARCHAR(30) NOT NULL DEFAULT 'LOCAL_ONLY' COMMENT '公开状态：LOCAL_ONLY、REVIEWING、PUBLIC_SAFE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_waht_asset_asset_type` (`asset_type`),
  KEY `idx_waht_asset_source_type` (`source_type`),
  KEY `idx_waht_asset_public_status` (`public_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材元数据表';

CREATE TABLE IF NOT EXISTS `waht_note` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` VARCHAR(120) NOT NULL COMMENT '标题',
  `slug` VARCHAR(150) NOT NULL COMMENT '访问标识，唯一',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '摘要',
  `content` LONGTEXT NOT NULL COMMENT '正文',
  `category_id` BIGINT NOT NULL COMMENT '分类 ID',
  `cover_asset_id` BIGINT DEFAULT NULL COMMENT '封面素材 ID',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT、PUBLISHED、HIDDEN',
  `view_count` BIGINT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `published_at` DATETIME DEFAULT NULL COMMENT '发布时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建用户 ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新用户 ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_waht_note_slug` (`slug`),
  KEY `idx_waht_note_category_id` (`category_id`),
  KEY `idx_waht_note_status_published_at` (`status`, `published_at`),
  KEY `idx_waht_note_cover_asset_id` (`cover_asset_id`),
  KEY `idx_waht_note_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习笔记表';

CREATE TABLE IF NOT EXISTS `waht_note_tag_rel` (
  `note_id` BIGINT NOT NULL COMMENT '笔记 ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签 ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`note_id`, `tag_id`),
  KEY `idx_waht_note_tag_rel_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记标签关联表';

CREATE TABLE IF NOT EXISTS `waht_project` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(100) NOT NULL COMMENT '项目名称',
  `slug` VARCHAR(120) NOT NULL COMMENT '访问标识，唯一',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '项目摘要',
  `description` LONGTEXT COMMENT '项目详情',
  `cover_asset_id` BIGINT DEFAULT NULL COMMENT '封面素材 ID',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT、PUBLISHED、HIDDEN',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `started_at` DATE DEFAULT NULL COMMENT '项目开始日期',
  `ended_at` DATE DEFAULT NULL COMMENT '项目结束日期',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_waht_project_slug` (`slug`),
  KEY `idx_waht_project_status_sort` (`status`, `sort_order`),
  KEY `idx_waht_project_cover_asset_id` (`cover_asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目展示表';

CREATE TABLE IF NOT EXISTS `waht_project_link` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id` BIGINT NOT NULL COMMENT '项目 ID',
  `link_type` VARCHAR(20) NOT NULL COMMENT '链接类型：REPO、DEMO、DOC、VIDEO、OTHER',
  `title` VARCHAR(80) NOT NULL COMMENT '链接标题',
  `url` VARCHAR(500) NOT NULL COMMENT '链接地址',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_waht_project_link_project_id` (`project_id`),
  KEY `idx_waht_project_link_link_type` (`link_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目链接表';

CREATE TABLE IF NOT EXISTS `waht_tech_stack` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(50) NOT NULL COMMENT '技术名称',
  `slug` VARCHAR(80) NOT NULL COMMENT '技术访问标识，唯一',
  `tech_type` VARCHAR(30) NOT NULL COMMENT '类型：LANGUAGE、FRAMEWORK、DATABASE、TOOL、OTHER',
  `icon_url` VARCHAR(500) DEFAULT NULL COMMENT '图标地址',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE、HIDDEN',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_waht_tech_stack_slug` (`slug`),
  KEY `idx_waht_tech_stack_tech_type` (`tech_type`),
  KEY `idx_waht_tech_stack_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技术栈表';

CREATE TABLE IF NOT EXISTS `waht_project_tech_rel` (
  `project_id` BIGINT NOT NULL COMMENT '项目 ID',
  `tech_id` BIGINT NOT NULL COMMENT '技术栈 ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`project_id`, `tech_id`),
  KEY `idx_waht_project_tech_rel_tech_id` (`tech_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目技术栈关联表';
