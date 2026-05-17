CREATE DATABASE IF NOT EXISTS `taskdb`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `taskdb`;

-- =============================================
-- 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS `tb_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码（bcrypt）',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER / ADMIN',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 任务表
-- =============================================
CREATE TABLE IF NOT EXISTS `task` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `title`       VARCHAR(200) NOT NULL COMMENT '任务标题',
    `description` TEXT         DEFAULT NULL COMMENT '任务描述',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'TODO' COMMENT '状态：TODO / IN_PROGRESS / DONE',
    `priority`    VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级：LOW / MEDIUM / HIGH',
    `deadline`    DATETIME     DEFAULT NULL COMMENT '截止时间',
    `user_id`     BIGINT       NOT NULL COMMENT '所属用户ID',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX `idx_task_user_id` (`user_id`),
    INDEX `idx_task_status` (`status`),
    INDEX `idx_task_priority` (`priority`),
    INDEX `idx_task_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- =============================================
-- 新闻表
-- =============================================
CREATE TABLE IF NOT EXISTS `news` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `title`        VARCHAR(500) NOT NULL COMMENT '新闻标题',
    `summary`      TEXT         DEFAULT NULL COMMENT '新闻摘要',
    `url`          VARCHAR(1000) DEFAULT NULL COMMENT '原文链接',
    `source`       VARCHAR(100) DEFAULT NULL COMMENT '来源',
    `published_at` DATETIME     DEFAULT NULL COMMENT '发布时间',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '抓取时间',
    INDEX `idx_news_source` (`source`),
    INDEX `idx_news_published_at` (`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技术新闻表';

-- =============================================
-- 任务-新闻关联表
-- =============================================
CREATE TABLE IF NOT EXISTS `task_news_relation` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `task_id`    BIGINT   NOT NULL COMMENT '任务ID',
    `news_id`    BIGINT   NOT NULL COMMENT '新闻ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关联时间',
    INDEX `idx_tnr_task_id` (`task_id`),
    INDEX `idx_tnr_news_id` (`news_id`),
    UNIQUE KEY `uk_task_news` (`task_id`, `news_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务-新闻关联表';

-- 默认用户通过应用注册接口创建，不再预置
