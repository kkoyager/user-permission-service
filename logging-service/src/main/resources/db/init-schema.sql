-- 日志服务数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS logging_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE logging_db;

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    ip VARCHAR(15) COMMENT 'IP地址',
    detail TEXT COMMENT '操作详情',
    gmt_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_create_time (gmt_create)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
