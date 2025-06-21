# Logging Service - 日志服务

## 项目概述

Logging Service 是一个专门用于处理操作日志的微服务，负责接收、存储和查询系统中的各种操作日志。该服务通过消息队列（RocketMQ）接收来自其他服务的日志消息，并提供RESTful API用于日志查询。

## 功能特性

- 🚀 **异步日志处理**：通过RocketMQ消息队列异步接收和处理日志消息
- 📊 **多维度查询**：支持按用户ID、时间范围、操作类型等条件查询日志
- 🔍 **分页查询**：支持大数据量的分页查询
- 💾 **持久化存储**：使用MySQL数据库存储日志数据
- 🛡️ **异常处理**：完善的异常处理和错误日志记录
- ⚡ **高性能**：基于Spring Boot构建，支持高并发处理

## 技术栈

- **Spring Boot 2.7.14** - 核心框架
- **Spring Data JPA** - 数据访问层
- **RocketMQ** - 消息队列
- **MySQL** - 数据库
- **Nacos** - 服务发现与配置管理
- **Lombok** - 代码简化
- **Jackson** - JSON处理
- **JUnit 5 + Mockito** - 单元测试

## 项目结构

```
logging-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── logging/
│   │   │           ├── LoggingServiceApplication.java     # 启动类
│   │   │           ├── config/
│   │   │           │   └── LoggingConfig.java             # 配置类
│   │   │           ├── consumer/
│   │   │           │   └── OperationLogConsumer.java      # MQ消费者
│   │   │           ├── controller/
│   │   │           │   └── LogController.java             # REST控制器
│   │   │           ├── entity/
│   │   │           │   └── OperationLog.java              # 日志实体
│   │   │           ├── event/
│   │   │           │   └── OperationLogEvent.java         # 日志事件
│   │   │           ├── exception/
│   │   │           │   └── GlobalExceptionHandler.java    # 全局异常处理
│   │   │           ├── repository/
│   │   │           │   └── OperationLogRepository.java    # 数据访问层
│   │   │           └── service/
│   │   │               ├── OperationLogService.java       # 服务接口
│   │   │               └── impl/
│   │   │                   └── OperationLogServiceImpl.java # 服务实现
│   │   └── resources/
│   │       ├── application.yml                            # 应用配置
│   │       └── db/
│   │           └── init-logging-service.sql               # 数据库初始化脚本
│   └── test/                                              # 测试代码
├── pom.xml                                                # Maven配置
└── README.md                                              # 项目文档
```

## 快速开始

### 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- RocketMQ 4.9+
- Nacos 2.0+

### 启动步骤

1. **启动基础服务**
   ```bash
   # 启动Nacos
   startup.cmd -m standalone
   
   # 启动RocketMQ NameServer
   mqnamesrv
   
   # 启动RocketMQ Broker
   mqbroker -n localhost:9876
   ```

2. **数据库初始化**
   ```sql
   -- 执行 src/main/resources/db/init-logging-service.sql
   CREATE DATABASE logging_service;
   USE logging_service;
   -- 运行SQL脚本
   ```

3. **配置修改**
   ```yaml
   # 修改 application.yml 中的数据库连接信息
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/logging_service
       username: your_username
       password: your_password
   ```

4. **启动服务**
   ```bash
   mvn spring-boot:run
   ```

## API 文档

### 1. 查询用户操作日志

**接口地址：** `GET /api/logs/user/{userId}`

**请求参数：**
- `userId` (Path参数): 用户ID

**响应示例：**
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
  "message": "查询成功"
}
```

### 2. 分页查询操作日志

**接口地址：** `GET /api/logs`

**请求参数：**
- `page` (Query参数): 页码，从0开始
- `size` (Query参数): 每页大小
- `userId` (Query参数): 用户ID（可选）
- `startTime` (Query参数): 开始时间（可选）
- `endTime` (Query参数): 结束时间（可选）

**响应示例：**
```json
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0
  },
  "message": "查询成功"
}
```

## 消息格式

### 操作日志消息

**Topic:** `operation-log-topic`

**消息格式：**
```json
{
  "userId": 123,
  "action": "创建用户",
  "ip": "192.168.1.1",
  "detail": "创建了用户：张三"
}
```

**字段说明：**
- `userId`: 操作用户ID
- `action`: 操作类型/名称
- `ip`: 操作者IP地址
- `detail`: 操作详细描述

## 数据库设计

### operation_log 表

```sql
CREATE TABLE operation_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    action VARCHAR(100) NOT NULL COMMENT '操作类型',
    ip VARCHAR(50) COMMENT 'IP地址',
    detail TEXT COMMENT '操作详情',
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_action (action)
) COMMENT '操作日志表';
```

## 配置说明

### 核心配置项

```yaml
server:
  port: 8083

spring:
  application:
    name: logging-service
  
  # 数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/logging_service?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  # JPA配置
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    database-platform: org.hibernate.dialect.MySQL8Dialect

  # Nacos配置
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848

# RocketMQ配置
rocketmq:
  name-server: localhost:9876
  producer:
    group: logging-service-producer
  consumer:
    group: logging-service-group
```

## 监控和运维

### 健康检查

```bash
# 检查服务状态
curl http://localhost:8083/actuator/health
```

### 日志查看

```bash
# 查看应用日志
tail -f logs/logging-service.log
```

### 性能监控

- **JVM监控**: 使用Spring Boot Actuator
- **数据库监控**: 监控连接池状态
- **消息队列监控**: 监控消费延迟和堆积

## 开发指南

### 添加新的日志类型

1. 在 `OperationLogEvent` 中添加新字段
2. 更新数据库表结构
3. 修改消费者处理逻辑
4. 添加相应的测试用例

### 扩展查询条件

1. 在 `OperationLogRepository` 中添加新的查询方法
2. 在 `OperationLogService` 中添加业务逻辑
3. 在 `LogController` 中暴露新的API
4. 编写单元测试

## 测试

### 运行单元测试

```bash
mvn test
```

### 测试覆盖率

```bash
mvn jacoco:report
```

### 集成测试

```bash
mvn verify
```

## 部署

### Docker部署

```dockerfile
FROM openjdk:11-jre-slim
COPY target/logging-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: logging-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: logging-service
  template:
    metadata:
      labels:
        app: logging-service
    spec:
      containers:
      - name: logging-service
        image: logging-service:latest
        ports:
        - containerPort: 8083
```

## 故障排查

### 常见问题

1. **RocketMQ连接失败**
   - 检查NameServer是否启动
   - 检查网络连接
   - 确认配置是否正确

2. **数据库连接失败**
   - 检查数据库服务状态
   - 验证连接字符串
   - 确认用户权限

3. **消息消费延迟**
   - 检查消费者线程数
   - 监控数据库性能
   - 查看错误日志

## 版本历史

- **v0.0.1** (2024-06-21)
  - 初始版本
  - 基础日志收集和查询功能
  - RocketMQ消息消费
  - RESTful API

## 贡献指南

1. Fork 项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证。
