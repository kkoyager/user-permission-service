# 用户服务 (User Service)

## 项目介绍
用户服务是微服务架构中负责用户管理和认证的核心服务，提供用户注册、登录、信息管理等功能。

## 主要功能
- 用户注册/登录认证(JWT)
- 用户信息管理
- 分库分表存储
- 权限校验
- 操作日志记录

## 技术栈
- Spring Boot 2.7.18
- Spring Cloud 2021.0.8  
- Nacos 服务发现
- OpenFeign RPC调用
- ShardingSphere 分库分表
- RocketMQ 消息队列
- Seata 分布式事务
- JWT 认证
- MySQL 数据库

## 快速开始

### 环境要求
- JDK 11+
- MySQL 8.0+
- Nacos 2.3.2
- RocketMQ 5.3.3

### 启动步骤
1. 启动基础组件(Nacos、RocketMQ、MySQL)
2. 执行数据库初始化脚本
3. 修改配置文件中的数据库连接信息
4. 运行主类 UserServiceApplication

## API接口

### 用户注册
```
POST /user/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

### 用户登录
```
POST /user/login
Content-Type: application/json

{
  "username": "testuser", 
  "password": "123456"
}
```

### 查询用户信息
```
GET /user/{userId}
Authorization: Bearer {token}
```

### 更新用户信息
```
PUT /user/{userId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "email": "new@example.com",
  "phone": "13900139000"
}
```

## 开发说明

### 分库分表策略
用户表按user_id进行水平分片，分片规则: user_id % 2

### 权限控制
- 普通用户：只能操作自己的信息
- 管理员：可操作普通用户信息
- 超级管理员：可操作所有用户信息

### 日志记录
所有关键操作通过MQ异步记录到日志服务
