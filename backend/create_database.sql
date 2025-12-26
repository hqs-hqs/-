-- ============================================
-- 游戏数据库初始化脚本
-- ============================================
-- 说明：
-- 1. 只需要执行此脚本创建数据库
-- 2. 数据表会在后端启动时自动创建（由 Spring Boot JPA 自动管理）
-- 3. 配置：spring.jpa.hibernate.ddl-auto=update
-- ============================================

-- 创建数据库 game_db
CREATE DATABASE IF NOT EXISTS game_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 使用数据库（可选，用于验证）
USE game_db;

-- 验证数据库创建成功（可选）
-- SHOW DATABASES LIKE 'game_db';

