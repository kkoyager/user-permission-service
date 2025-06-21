-- 用户数据库0初始化脚本
CREATE DATABASE IF NOT EXISTS user_db_0 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE user_db_0;

-- 用户表（分库分表）
CREATE TABLE IF NOT EXISTS users (
  user_id BIGINT PRIMARY KEY COMMENT '用户ID，使用雪花算法生成',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(255) NOT NULL COMMENT '密码',
  email VARCHAR(100) COMMENT '邮箱',
  phone VARCHAR(20) COMMENT '手机号',
  gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  gmt_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  INDEX idx_username (username),
  INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表-分片0';
