-- WAHT local admin user
-- 默认本地账号：admin
-- 默认本地密码：admin123
-- 说明：password 字段存储 BCrypt 哈希，不存储明文密码。

USE `waht`;

INSERT INTO `waht_user` (
  `username`,
  `password`,
  `nickname`,
  `avatar_url`,
  `role`,
  `status`,
  `created_at`,
  `updated_at`
)
SELECT
  'admin',
  '$2a$10$JSNxL5co8bLx5mVUK/OqrelgvgQGyReKcJwHnnAJEu6hmyC33I8ba',
  'WAHT Admin',
  NULL,
  'ADMIN',
  'ACTIVE',
  NOW(),
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `waht_user` WHERE `username` = 'admin'
);
