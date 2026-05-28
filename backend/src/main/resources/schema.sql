-- AgentHub Database Schema
-- MySQL 8.0

CREATE DATABASE IF NOT EXISTS agenthub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE agenthub;

-- User Table
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(100) NOT NULL,
    `avatar_url` VARCHAR(500) DEFAULT NULL,
    `email` VARCHAR(100) DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Agent Provider Table
CREATE TABLE IF NOT EXISTS `agent_provider` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `api_base_url` VARCHAR(500) NOT NULL,
    `api_key` VARCHAR(255) NOT NULL,
    `enabled` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Agent Table
CREATE TABLE IF NOT EXISTS `agent` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `avatar_url` VARCHAR(500) DEFAULT NULL,
    `system_prompt` TEXT NOT NULL,
    `provider` VARCHAR(50) NOT NULL,
    `provider_agent_id` VARCHAR(100) DEFAULT NULL,
    `model` VARCHAR(100) DEFAULT NULL,
    `tools` JSON DEFAULT NULL,
    `owner_id` BIGINT DEFAULT NULL,
    `is_public` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_owner` (`owner_id`),
    INDEX `idx_public` (`is_public`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Conversation Table
CREATE TABLE IF NOT EXISTS `conversation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(200) NOT NULL,
    `type` TINYINT NOT NULL COMMENT '1=single, 2=group',
    `owner_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `last_message_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_owner` (`owner_id`),
    INDEX `idx_updated` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Conversation Participant Table
CREATE TABLE IF NOT EXISTS `conversation_participant` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `conversation_id` BIGINT NOT NULL,
    `user_id` BIGINT DEFAULT NULL,
    `agent_id` BIGINT DEFAULT NULL,
    `role` TINYINT NOT NULL COMMENT '1=owner, 2=member',
    `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_conv` (`conversation_id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_agent` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Message Table
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `conversation_id` BIGINT NOT NULL,
    `sender_id` BIGINT NOT NULL,
    `sender_type` TINYINT NOT NULL COMMENT '1=user, 2=agent, 3=orchestrator',
    `content` TEXT NOT NULL,
    `message_type` TINYINT NOT NULL COMMENT '1=text, 2=artifact, 3=system',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_conv` (`conversation_id`),
    INDEX `idx_created` (`created_at`),
    INDEX `idx_conv_created` (`conversation_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Message Block (Artifact) Table
CREATE TABLE IF NOT EXISTS `message_block` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `message_id` BIGINT NOT NULL,
    `block_type` VARCHAR(50) NOT NULL COMMENT 'code/diff/web/file/deploy',
    `content` LONGTEXT NOT NULL,
    `language` VARCHAR(50) DEFAULT NULL,
    `title` VARCHAR(200) DEFAULT NULL,
    `metadata` JSON DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_message` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Task Table (for Orchestrator)
CREATE TABLE IF NOT EXISTS `task` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `conversation_id` BIGINT NOT NULL,
    `parent_message_id` BIGINT NOT NULL,
    `agent_id` BIGINT NOT NULL,
    `status` TINYINT NOT NULL COMMENT '1=pending, 2=running, 3=completed, 4=failed',
    `result` TEXT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `completed_at` DATETIME DEFAULT NULL,
    INDEX `idx_conv` (`conversation_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Deployment Record Table
CREATE TABLE IF NOT EXISTS `deployment_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `message_block_id` BIGINT NOT NULL,
    `status` VARCHAR(50) DEFAULT 'pending',
    `deployment_url` VARCHAR(500) DEFAULT NULL,
    `logs` TEXT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default system agents
INSERT INTO `agent` (`name`, `description`, `system_prompt`, `provider`, `is_public`) VALUES
('Code Assistant', '专业的代码助手', '你是一个专业的代码助手，帮助用户编写、调试和优化代码。', 'custom', 1),
('Doc Writer', '文档写作助手', '你是一个专业的文档写作助手，帮助用户撰写各类文档。', 'custom', 1);
