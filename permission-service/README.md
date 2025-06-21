# 权限服务 README

## 项目简介

权限服务是一个基于Spring Boot的微服务，提供用户角色管理功能，包括角色绑定、查询、升级和降级等操作。

## 快速开始

### 环境要求
- Java 8+
- MySQL 8.0+
- Maven 3.6+

### 安装与运行

1. **克隆项目**
```bash
git clone <repository-url>
cd permission-service
```

2. **配置数据库**
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE permission_db;
```

3. **配置环境变量**
```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

4. **构建项目**
```bash
mvn clean install
```

5. **运行应用**
```bash
mvn spring-boot:run
```

应用启动后访问：http://localhost:8081

### 快速测试

```bash
# 运行单元测试
mvn test

# 运行特定测试类
mvn test -Dtest=PermissionServiceImplTest
```

## 核心功能

- 🔐 **默认角色绑定** - 为新用户自动分配普通用户角色
- 🔍 **角色查询** - 根据用户ID查询角色代码
- ⬆️ **权限升级** - 将普通用户升级为管理员
- ⬇️ **权限降级** - 将管理员降级为普通用户

## API示例

```bash
# 绑定默认角色
curl -X POST http://localhost:8081/rpc/permission/bind-default-role \
  -H "Content-Type: application/json" \
  -d '{"userId": 1001}'

# 查询用户角色
curl http://localhost:8081/rpc/permission/role-code/1001

# 升级为管理员
curl -X PUT http://localhost:8081/rpc/permission/upgrade-admin/1001

# 降级为普通用户
curl -X PUT http://localhost:8081/rpc/permission/downgrade-user/1001
```

## 项目结构

```
src/
├── main/java/com/permission/
│   ├── controller/          # REST控制器
│   ├── service/            # 业务逻辑层
│   ├── repository/         # 数据访问层
│   ├── entity/            # 实体类
│   └── exception/         # 异常处理
├── main/resources/
│   ├── application.yml    # 应用配置
│   └── db/init-logging-service.sql # 数据库脚本
└── test/                  # 测试代码
```

## 文档

- 📖 [功能文档](docs/权限服务功能文档.md) - 详细的功能说明和API文档
- 🧪 [测试文档](docs/测试文档.md) - 测试用例说明和测试指南

## 技术栈

- **后端框架**: Spring Boot 2.x
- **数据库**: MySQL 8.0
- **ORM**: JPA/Hibernate
- **测试**: JUnit 5 + Mockito
- **构建工具**: Maven
- **日志**: SLF4J + Logback

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 项目维护者：权限服务开发组
- 邮箱：permission-service@company.com
