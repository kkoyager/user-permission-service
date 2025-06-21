# User Service API 文档

## 接口概述

User Service 提供用户管理相关的 REST API 接口，包括用户注册、登录、信息查询、更新等功能。所有接口返回统一的 JSON 格式响应。

## 基础信息

- **服务名称**: user-service
- **默认端口**: 8081
- **基础路径**: /user
- **API版本**: v1.0
- **协议**: HTTP/HTTPS

## 通用响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 具体业务数据
  },
  "timestamp": "2024-06-21T10:30:00"
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "错误信息描述",
  "data": null,
  "timestamp": "2024-06-21T10:30:00"
}
```

### 状态码说明

| 状态码 | 说明 | 示例 |
|--------|------|------|
| 200 | 操作成功 | 查询、创建、更新成功 |
| 400 | 客户端错误 | 参数错误、业务规则违反 |
| 401 | 未认证 | Token无效或过期 |
| 403 | 权限不足 | 无权限执行当前操作 |
| 404 | 资源不存在 | 用户不存在 |
| 500 | 服务器错误 | 系统内部错误 |

## 认证机制

### JWT Token认证

大部分接口需要在请求头中携带JWT令牌：

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Token获取

通过登录接口获取JWT令牌：

```http
POST /user/login
```

### Token刷新

Token有效期为24小时，过期后需要重新登录获取新的Token。

## API接口详情

### 1. 用户注册

**接口地址**: `POST /user/register`

**接口描述**: 注册新用户账号

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| username | String | 是 | 用户名，3-20字符 | "testuser" |
| password | String | 是 | 密码，6-50字符 | "123456" |
| email | String | 是 | 邮箱地址 | "test@example.com" |
| phone | String | 否 | 手机号码 | "13800138000" |

**请求示例**:
```http
POST /user/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1001,
    "username": "testuser",
    "email": "test@example.com",
    "phone": "13800138000",
    "createdAt": "2024-06-21T10:30:00"
  },
  "timestamp": "2024-06-21T10:30:00"
}
```

失败响应：
```json
{
  "code": 400,
  "message": "用户名已存在",
  "data": null,
  "timestamp": "2024-06-21T10:30:00"
}
```

**错误码说明**:

| 错误码 | 错误信息 | 说明 |
|--------|----------|------|
| 400 | 用户名已存在 | 用户名重复 |
| 400 | 邮箱已存在 | 邮箱重复 |
| 400 | 密码格式不正确，长度应为6-50位 | 密码不符合规则 |

---

### 2. 用户登录

**接口地址**: `POST /user/login`

**接口描述**: 用户登录获取访问令牌

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| username | String | 是 | 用户名 | "testuser" |
| password | String | 是 | 密码 | "123456" |

**请求示例**:
```http
POST /user/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}
```

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMDAx...",
  "timestamp": "2024-06-21T10:30:00"
}
```

失败响应：
```json
{
  "code": 400,
  "message": "用户名或密码错误",
  "data": null,
  "timestamp": "2024-06-21T10:30:00"
}
```

---

### 3. 查询用户信息

**接口地址**: `GET /user/{userId}`

**接口描述**: 根据用户ID查询用户详细信息

**认证要求**: 需要JWT Token

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| userId | Long | 是 | 用户ID | 1001 |

**权限说明**:
- 用户只能查询自己的信息
- 管理员可以查询任意用户信息

**请求示例**:
```http
GET /user/1001
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1001,
    "username": "testuser",
    "email": "test@example.com",
    "phone": "13800138000",
    "createdAt": "2024-06-21T10:30:00",
    "updatedAt": "2024-06-21T10:30:00"
  },
  "timestamp": "2024-06-21T10:30:00"
}
```

失败响应：
```json
{
  "code": 403,
  "message": "没有权限查看该用户信息",
  "data": null,
  "timestamp": "2024-06-21T10:30:00"
}
```

**错误码说明**:

| 错误码 | 错误信息 | 说明 |
|--------|----------|------|
| 401 | 请先登录 | 未携带Token或Token无效 |
| 403 | 没有权限查看该用户信息 | 权限不足 |
| 400 | 用户不存在 | 用户ID不存在 |

---

### 4. 分页查询用户列表

**接口地址**: `GET /user/users`

**接口描述**: 分页查询用户列表

**认证要求**: 需要JWT Token

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 | 示例 |
|--------|------|------|--------|------|------|
| page | Integer | 否 | 0 | 页码，从0开始 | 0 |
| size | Integer | 否 | 10 | 每页大小 | 10 |

**权限说明**:
- 普通用户只能查看自己的信息（返回只包含自己的列表）
- 管理员可以查看所有用户列表

**请求示例**:
```http
GET /user/users?page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "content": [
      {
        "userId": 1001,
        "username": "testuser",
        "email": "test@example.com",
        "phone": "13800138000",
        "createdAt": "2024-06-21T10:30:00"
      },
      {
        "userId": 1002,
        "username": "admin",
        "email": "admin@example.com",
        "phone": "13900139000",
        "createdAt": "2024-06-21T11:00:00"
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
    "totalElements": 2,
    "totalPages": 1,
    "size": 10,
    "number": 0,
    "numberOfElements": 2,
    "first": true,
    "last": true
  },
  "timestamp": "2024-06-21T10:30:00"
}
```

---

### 5. 更新用户信息

**接口地址**: `PUT /user/{userId}`

**接口描述**: 更新用户基本信息

**认证要求**: 需要JWT Token

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| userId | Long | 是 | 用户ID | 1001 |

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| email | String | 否 | 新邮箱地址 | "newemail@example.com" |
| phone | String | 否 | 新手机号码 | "13900139000" |

**权限说明**:
- 用户只能更新自己的信息
- 管理员可以更新任意用户信息
- 用户名和密码不能通过此接口修改

**请求示例**:
```http
PUT /user/1001
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "email": "newemail@example.com",
  "phone": "13900139000"
}
```

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "userId": 1001,
    "username": "testuser",
    "email": "newemail@example.com",
    "phone": "13900139000",
    "createdAt": "2024-06-21T10:30:00",
    "updatedAt": "2024-06-21T12:00:00"
  },
  "timestamp": "2024-06-21T12:00:00"
}
```

失败响应：
```json
{
  "code": 403,
  "message": "没有权限更新该用户信息",
  "data": null,
  "timestamp": "2024-06-21T12:00:00"
}
```

---

### 6. 重置密码

**接口地址**: `POST /user/reset-password`

**接口描述**: 重置用户密码（仅管理员可操作）

**认证要求**: 需要JWT Token（管理员权限）

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| userId | Long | 是 | 目标用户ID | 1002 |
| newPassword | String | 是 | 新密码，6-50字符 | "newpassword123" |

**权限说明**:
- 仅管理员可以执行此操作
- 不能重置超级管理员的密码

**请求示例**:
```http
POST /user/reset-password
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "userId": 1002,
  "newPassword": "newpassword123"
}
```

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": "密码重置成功",
  "timestamp": "2024-06-21T12:00:00"
}
```

失败响应：
```json
{
  "code": 403,
  "message": "没有权限重置该用户密码",
  "data": null,
  "timestamp": "2024-06-21T12:00:00"
}
```

**错误码说明**:

| 错误码 | 错误信息 | 说明 |
|--------|----------|------|
| 403 | 没有权限重置该用户密码 | 当前用户不是管理员 |
| 400 | 用户不存在 | 目标用户ID不存在 |
| 400 | 密码格式不正确，长度应为6-50位 | 新密码不符合规则 |

---

### 7. 获取当前用户角色

**接口地址**: `GET /user/current-role`

**接口描述**: 获取当前登录用户的角色信息

**认证要求**: 需要JWT Token

**请求示例**:
```http
GET /user/current-role
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "获取角色成功",
  "data": "admin",
  "timestamp": "2024-06-21T12:00:00"
}
```

**角色说明**:

| 角色代码 | 角色名称 | 权限说明 |
|----------|----------|----------|
| user | 普通用户 | 只能操作自己的信息 |
| admin | 管理员 | 可以操作普通用户的信息 |
| super_admin | 超级管理员 | 可以操作所有用户的信息 |

---

### 8. 健康检查

**接口地址**: `GET /user/health`

**接口描述**: 检查服务健康状态

**认证要求**: 无

**请求示例**:
```http
GET /user/health
```

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "用户服务运行正常",
  "data": null,
  "timestamp": "2024-06-21T12:00:00"
}
```

## 数据模型

### UserRegisterRequest

用户注册请求模型：

```json
{
  "username": "string",    // 用户名，3-20字符，必填
  "password": "string",    // 密码，6-50字符，必填
  "email": "string",       // 邮箱地址，必填
  "phone": "string"        // 手机号码，可选
}
```

### UserLoginRequest

用户登录请求模型：

```json
{
  "username": "string",    // 用户名，必填
  "password": "string"     // 密码，必填
}
```

### UserUpdateRequest

用户更新请求模型：

```json
{
  "email": "string",       // 新邮箱地址，可选
  "phone": "string"        // 新手机号码，可选
}
```

### UserResponse

用户信息响应模型：

```json
{
  "userId": "long",        // 用户ID
  "username": "string",    // 用户名
  "email": "string",       // 邮箱地址
  "phone": "string",       // 手机号码
  "createdAt": "datetime", // 创建时间
  "updatedAt": "datetime"  // 更新时间
}
```

### ResetPasswordRequest

密码重置请求模型：

```json
{
  "userId": "long",        // 目标用户ID，必填
  "newPassword": "string"  // 新密码，6-50字符，必填
}
```

## 错误处理

### 业务错误

所有业务错误都返回HTTP 200状态码，通过响应体中的code字段区分：

```json
{
  "code": 400,
  "message": "具体错误信息",
  "data": null,
  "timestamp": "2024-06-21T12:00:00"
}
```

### 系统错误

系统级错误返回对应的HTTP状态码：

| HTTP状态码 | 说明 | 场景 |
|------------|------|------|
| 404 | 接口不存在 | 请求了不存在的API |
| 405 | 方法不允许 | 使用错误的HTTP方法 |
| 415 | 媒体类型不支持 | Content-Type错误 |
| 500 | 内部服务器错误 | 系统异常 |

### 参数验证错误

参数验证失败时的响应：

```json
{
  "code": 400,
  "message": "参数验证失败: 用户名不能为空",
  "data": null,
  "timestamp": "2024-06-21T12:00:00"
}
```

## 使用示例

### JavaScript/TypeScript

```javascript
// 用户注册
async function registerUser(userData) {
  try {
    const response = await fetch('/user/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData)
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
      console.log('注册成功:', result.data);
    } else {
      console.error('注册失败:', result.message);
    }
  } catch (error) {
    console.error('请求失败:', error);
  }
}

// 用户登录
async function loginUser(credentials) {
  try {
    const response = await fetch('/user/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials)
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
      // 保存Token
      localStorage.setItem('token', result.data);
      console.log('登录成功');
    } else {
      console.error('登录失败:', result.message);
    }
  } catch (error) {
    console.error('请求失败:', error);
  }
}

// 获取用户信息
async function getUserInfo(userId) {
  try {
    const token = localStorage.getItem('token');
    const response = await fetch(`/user/${userId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
      }
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
      console.log('用户信息:', result.data);
      return result.data;
    } else {
      console.error('获取失败:', result.message);
    }
  } catch (error) {
    console.error('请求失败:', error);
  }
}
```

### Java (使用RestTemplate)

```java
@Service
public class UserServiceClient {
    
    private final RestTemplate restTemplate;
    private final String baseUrl = "http://localhost:8081";
    
    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    // 用户注册
    public ApiResponse<UserResponse> register(UserRegisterRequest request) {
        String url = baseUrl + "/user/register";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<UserRegisterRequest> entity = new HttpEntity<>(request, headers);
        
        return restTemplate.postForObject(url, entity, 
            new ParameterizedTypeReference<ApiResponse<UserResponse>>() {});
    }
    
    // 用户登录
    public ApiResponse<String> login(UserLoginRequest request) {
        String url = baseUrl + "/user/login";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<UserLoginRequest> entity = new HttpEntity<>(request, headers);
        
        return restTemplate.postForObject(url, entity, 
            new ParameterizedTypeReference<ApiResponse<String>>() {});
    }
    
    // 获取用户信息
    public ApiResponse<UserResponse> getUserInfo(Long userId, String token) {
        String url = baseUrl + "/user/" + userId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        return restTemplate.exchange(url, HttpMethod.GET, entity, 
            new ParameterizedTypeReference<ApiResponse<UserResponse>>() {}).getBody();
    }
}
```

### Python (使用requests)

```python
import requests
import json

class UserServiceClient:
    def __init__(self, base_url="http://localhost:8081"):
        self.base_url = base_url
        self.token = None
    
    def register(self, username, password, email, phone=None):
        """用户注册"""
        url = f"{self.base_url}/user/register"
        data = {
            "username": username,
            "password": password,
            "email": email,
            "phone": phone
        }
        
        response = requests.post(url, json=data)
        return response.json()
    
    def login(self, username, password):
        """用户登录"""
        url = f"{self.base_url}/user/login"
        data = {
            "username": username,
            "password": password
        }
        
        response = requests.post(url, json=data)
        result = response.json()
        
        if result["code"] == 200:
            self.token = result["data"]
        
        return result
    
    def get_user_info(self, user_id):
        """获取用户信息"""
        if not self.token:
            raise Exception("请先登录")
        
        url = f"{self.base_url}/user/{user_id}"
        headers = {
            "Authorization": f"Bearer {self.token}"
        }
        
        response = requests.get(url, headers=headers)
        return response.json()
    
    def update_user(self, user_id, email=None, phone=None):
        """更新用户信息"""
        if not self.token:
            raise Exception("请先登录")
        
        url = f"{self.base_url}/user/{user_id}"
        headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }
        
        data = {}
        if email:
            data["email"] = email
        if phone:
            data["phone"] = phone
        
        response = requests.put(url, json=data, headers=headers)
        return response.json()

# 使用示例
client = UserServiceClient()

# 注册用户
result = client.register("testuser", "123456", "test@example.com", "13800138000")
print(f"注册结果: {result}")

# 登录
result = client.login("testuser", "123456")
print(f"登录结果: {result}")

# 获取用户信息
if client.token:
    result = client.get_user_info(1001)
    print(f"用户信息: {result}")
```

## 测试工具

### Postman集合

可以导入以下Postman集合进行API测试：

```json
{
  "info": {
    "name": "User Service API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8081"
    },
    {
      "key": "token",
      "value": ""
    }
  ],
  "item": [
    {
      "name": "用户注册",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"username\": \"testuser\",\n  \"password\": \"123456\",\n  \"email\": \"test@example.com\",\n  \"phone\": \"13800138000\"\n}"
        },
        "url": "{{baseUrl}}/user/register"
      }
    },
    {
      "name": "用户登录",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"username\": \"testuser\",\n  \"password\": \"123456\"\n}"
        },
        "url": "{{baseUrl}}/user/login"
      }
    }
  ]
}
```

### cURL命令

```bash
# 用户注册
curl -X POST http://localhost:8081/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "email": "test@example.com",
    "phone": "13800138000"
  }'

# 用户登录
curl -X POST http://localhost:8081/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'

# 获取用户信息（需要替换TOKEN）
curl -X GET http://localhost:8081/user/1001 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 更新用户信息
curl -X PUT http://localhost:8081/user/1001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "email": "newemail@example.com",
    "phone": "13900139000"
  }'
```

## 版本更新日志

### v1.0.0 (2024-06-21)
- ✅ 用户注册接口
- ✅ 用户登录接口
- ✅ 用户信息查询接口
- ✅ 分页查询用户列表接口
- ✅ 用户信息更新接口
- ✅ 密码重置接口
- ✅ 获取当前用户角色接口
- ✅ 健康检查接口
- ✅ JWT认证机制
- ✅ 基于角色的权限控制

## 联系方式

如有API相关问题或建议，请联系开发团队。

---

*最后更新时间: 2024-06-21*
