CREATE DATABASE IF NOT EXISTS permission_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE permission_db;

-- 角色表
CREATE TABLE IF NOT EXISTS roles (  
  role_id INT PRIMARY KEY,  -- 1:超管 2:普通用户 3:管理员  
  role_code VARCHAR(20) UNIQUE  -- super_admin/user/admin  
);  

-- 用户-角色关系表  
CREATE TABLE IF NOT EXISTS user_roles (  
  id BIGINT PRIMARY KEY AUTO_INCREMENT,  
  user_id BIGINT,  
  role_id INT,  
  UNIQUE KEY uk_user_role (user_id)  -- 每个用户仅绑定一个角色  
);

-- 初始化角色数据
INSERT INTO roles (role_id, role_code) VALUES 
(1, 'super_admin'),
(2, 'user'),
(3, 'admin');

-- 初始化一个超级管理员 (假设用户ID为1)
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
