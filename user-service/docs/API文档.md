# 用户服务API文档

## 概述
用户服务提供用户注册、登录、信息管理等功能，支持JWT认证和角色权限控制。

## 基础信息
- **服务名称**: user-service
- **端口**: 8080
- **基础路径**: /user
- **认证方式**: JWT Bearer Token

## API接口列表

### 1. 用户注册
**接口路径**: `POST /user/register`

**请求示例**:
```json
{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1001,
    "username": "testuser",
    "email": "test@example.com",
    "phone": "13800138000",
    "gmtCreate": "2024-06-21T10:30:00",
    "gmtModified": "2024-06-21T10:30:00"
  }
}
```

### 2. 用户登录
**接口路径**: `POST /user/login`

**请求示例**:
```json
{
  "username": "testuser",
  "password": "123456"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDAxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImlhdCI6MTYyNDA..."
}
```

### 3. 查询用户信息
**接口路径**: `GET /user/{userId}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1001,
    "username": "testuser",
    "email": "test@example.com",
    "phone": "13800138000",
    "gmtCreate": "2024-06-21T10:30:00",
    "gmtModified": "2024-06-21T10:30:00"
  }
}
```

### 4. 分页查询用户列表
**接口路径**: `GET /user/users?page=0&size=10`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "userId": 1001,
        "username": "testuser",
        "email": "test@example.com",
        "phone": "13800138000",
        "gmtCreate": "2024-06-21T10:30:00",
        "gmtModified": "2024-06-21T10:30:00"
      }
    ],
    "pageable": {
      "sort": {
        "sorted": false,
        "unsorted": true
      },
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

### 5. 更新用户信息
**接口路径**: `PUT /user/{userId}`

**请求头**:
```
Authorization: Bearer {token}
```

**请求示例**:
```json
{
  "email": "newemail@example.com",
  "phone": "13900139000"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "userId": 1001,
    "username": "testuser",
    "email": "newemail@example.com",
    "phone": "13900139000",
    "gmtCreate": "2024-06-21T10:30:00",
    "gmtModified": "2024-06-21T11:00:00"
  }
}
```

### 6. 重置密码
**接口路径**: `POST /user/reset-password`

**请求头**:
```
Authorization: Bearer {token}
```

**请求示例**:
```json
{
  "userId": 1001,
  "newPassword": "newpassword123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

### 7. 获取当前用户角色
**接口路径**: `GET /user/current-role`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取角色成功",
  "data": "admin"
}
```

### 8. 健康检查
**接口路径**: `GET /user/health`

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "用户服务运行正常"
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数错误或业务异常 |
| 401 | 未认证，需要登录 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 系统内部错误 |
| 503 | 服务暂不可用 |

## 权限说明

### 角色类型
- **user**: 普通用户，只能操作自己的信息
- **admin**: 管理员，可以操作普通用户的信息
- **super_admin**: 超级管理员，可以操作所有用户的信息

### 权限矩阵

| 操作 | 普通用户 | 管理员 | 超级管理员 |
|------|----------|--------|------------|
| 查看自己信息 | ✓ | ✓ | ✓ |
| 查看其他用户信息 | ✗ | ✓(仅普通用户) | ✓ |
| 修改自己信息 | ✓ | ✓ | ✓ |
| 修改其他用户信息 | ✗ | ✓(仅普通用户) | ✓ |
| 重置自己密码 | ✓ | ✓ | ✓ |
| 重置其他用户密码 | ✗ | ✓(仅普通用户) | ✓ |
| 查看用户列表 | ✓(仅自己) | ✓(所有普通用户) | ✓(所有用户) |

## 技术特性

1. **分库分表**: 用户表按user_id进行水平分片
2. **分布式事务**: 用户注册时保证用户创建和角色绑定的原子性
3. **RPC调用**: 通过Feign调用权限服务获取用户角色
4. **异步日志**: 通过RocketMQ异步记录操作日志
5. **JWT认证**: 使用JWT进行用户身份验证
6. **密码加密**: 使用SHA-256+盐值加密存储密码
