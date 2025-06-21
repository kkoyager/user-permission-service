# Logging Service API 文档

## 概述

Logging Service 提供RESTful API用于查询操作日志，同时通过RocketMQ消息队列接收日志数据。

## 基础信息

- **服务地址**: http://localhost:8083
- **API版本**: v1
- **数据格式**: JSON
- **字符编码**: UTF-8

## 通用响应格式

所有API响应都遵循统一的格式：

```json
{
  "success": true,           // 请求是否成功
  "data": {},               // 响应数据
  "message": "操作成功",     // 响应消息
  "timestamp": "2024-06-21T10:30:00"  // 响应时间
}
```

## API 接口

### 1. 根据用户ID查询日志

**接口描述**: 查询指定用户的所有操作日志

**请求方式**: `GET`

**请求路径**: `/api/logs/user/{userId}`

**路径参数**:
| 参数名 | 类型 | 必填 | 描述 |
|-------|------|------|------|
| userId | Long | 是 | 用户ID |

**请求示例**:
```http
GET /api/logs/user/123
Host: localhost:8083
Accept: application/json
```

**响应示例**:
```json
{
  "success": true,
  "data": [
    {
      "logId": 1,
      "userId": 123,
      "action": "创建用户",
      "ip": "192.168.1.1",
      "detail": "创建了用户：张三",
      "timestamp": "2024-06-21T10:30:00"
    },
    {
      "logId": 2,
      "userId": 123,
      "action": "更新用户",
      "ip": "192.168.1.1",
      "detail": "更新了用户信息",
      "timestamp": "2024-06-21T11:00:00"
    }
  ],
  "message": "查询成功",
  "timestamp": "2024-06-21T12:00:00"
}
```

### 2. 分页查询日志

**接口描述**: 分页查询操作日志，支持多种筛选条件

**请求方式**: `GET`

**请求路径**: `/api/logs`

**查询参数**:
| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|-------|------|------|--------|------|
| page | Integer | 否 | 0 | 页码，从0开始 |
| size | Integer | 否 | 20 | 每页大小 |
| userId | Long | 否 | - | 用户ID筛选 |
| action | String | 否 | - | 操作类型筛选 |
| startTime | String | 否 | - | 开始时间(ISO格式) |
| endTime | String | 否 | - | 结束时间(ISO格式) |

**请求示例**:
```http
GET /api/logs?page=0&size=10&userId=123&action=创建用户&startTime=2024-06-20T00:00:00&endTime=2024-06-21T23:59:59
Host: localhost:8083
Accept: application/json
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "logId": 1,
        "userId": 123,
        "action": "创建用户",
        "ip": "192.168.1.1",
        "detail": "创建了用户：张三",
        "timestamp": "2024-06-21T10:30:00"
      }
    ],
    "pageable": {
      "sort": {
        "sorted": true,
        "unsorted": false
      },
      "pageNumber": 0,
      "pageSize": 10,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true,
    "first": true,
    "numberOfElements": 1,
    "size": 10,
    "number": 0,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "message": "查询成功",
  "timestamp": "2024-06-21T12:00:00"
}
```

### 3. 根据时间范围查询日志

**接口描述**: 查询指定时间范围内的操作日志

**请求方式**: `GET`

**请求路径**: `/api/logs/range`

**查询参数**:
| 参数名 | 类型 | 必填 | 描述 |
|-------|------|------|------|
| startTime | String | 是 | 开始时间(ISO格式) |
| endTime | String | 是 | 结束时间(ISO格式) |

**请求示例**:
```http
GET /api/logs/range?startTime=2024-06-21T00:00:00&endTime=2024-06-21T23:59:59
Host: localhost:8083
Accept: application/json
```

**响应示例**:
```json
{
  "success": true,
  "data": [
    {
      "logId": 1,
      "userId": 123,
      "action": "创建用户",
      "ip": "192.168.1.1",
      "detail": "创建了用户：张三",
      "timestamp": "2024-06-21T10:30:00"
    }
  ],
  "message": "查询成功",
  "timestamp": "2024-06-21T12:00:00"
}
```

## 错误响应

当请求出现错误时，响应格式如下：

```json
{
  "success": false,
  "data": null,
  "message": "错误信息描述",
  "timestamp": "2024-06-21T12:00:00"
}
```

### 常见错误码

| HTTP状态码 | 错误信息 | 描述 |
|-----------|----------|------|
| 400 | 请求参数错误 | 请求参数格式不正确或缺少必填参数 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 500 | 服务器内部错误 | 服务器处理请求时发生错误 |

## 消息队列接口

### 操作日志消息

**Topic**: `operation-log-topic`

**Consumer Group**: `logging-service-group`

**消息格式**:
```json
{
  "userId": 123,
  "action": "创建用户",
  "ip": "192.168.1.1",
  "detail": "创建了用户：张三"
}
```

**字段说明**:
| 字段名 | 类型 | 必填 | 描述 |
|-------|------|------|------|
| userId | Long | 是 | 操作用户ID |
| action | String | 是 | 操作类型/名称 |
| ip | String | 否 | 操作者IP地址 |
| detail | String | 否 | 操作详细描述 |

**消息发送示例** (其他服务发送日志消息):
```java
@Autowired
private RocketMQTemplate rocketMQTemplate;

public void sendOperationLog(OperationLogEvent event) {
    rocketMQTemplate.convertAndSend("operation-log-topic", event);
}
```

## 数据模型

### OperationLog (操作日志)

| 字段名 | 类型 | 描述 |
|-------|------|------|
| logId | Long | 日志ID，主键 |
| userId | Long | 用户ID |
| action | String | 操作类型 |
| ip | String | IP地址 |
| detail | String | 操作详情 |
| timestamp | LocalDateTime | 操作时间 |

## 使用示例

### JavaScript 调用示例

```javascript
// 查询用户日志
async function getUserLogs(userId) {
    const response = await fetch(`/api/logs/user/${userId}`);
    const result = await response.json();
    if (result.success) {
        console.log('用户日志:', result.data);
    } else {
        console.error('查询失败:', result.message);
    }
}

// 分页查询日志
async function getLogsWithPagination(page = 0, size = 20) {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString()
    });
    
    const response = await fetch(`/api/logs?${params}`);
    const result = await response.json();
    if (result.success) {
        console.log('分页数据:', result.data);
    } else {
        console.error('查询失败:', result.message);
    }
}
```

### Java 客户端示例

```java
// 使用RestTemplate调用API
@Autowired
private RestTemplate restTemplate;

public List<OperationLog> getUserLogs(Long userId) {
    String url = "http://logging-service/api/logs/user/" + userId;
    ResponseEntity<ApiResponse<List<OperationLog>>> response = 
        restTemplate.exchange(url, HttpMethod.GET, null, 
        new ParameterizedTypeReference<ApiResponse<List<OperationLog>>>() {});
    
    if (response.getBody().isSuccess()) {
        return response.getBody().getData();
    }
    throw new RuntimeException(response.getBody().getMessage());
}
```

### cURL 调用示例

```bash
# 查询用户日志
curl -X GET "http://localhost:8083/api/logs/user/123" \
     -H "Accept: application/json"

# 分页查询日志
curl -X GET "http://localhost:8083/api/logs?page=0&size=10&userId=123" \
     -H "Accept: application/json"

# 时间范围查询
curl -X GET "http://localhost:8083/api/logs/range?startTime=2024-06-21T00:00:00&endTime=2024-06-21T23:59:59" \
     -H "Accept: application/json"
```

## 性能说明

- **并发处理能力**: 支持1000+ QPS
- **消息处理延迟**: < 100ms
- **数据库查询响应**: < 200ms
- **分页查询限制**: 单次最大1000条记录

## 版本变更

### v1.0.0 (2024-06-21)
- 初始版本发布
- 支持基本的日志查询功能
- 支持RocketMQ消息消费
