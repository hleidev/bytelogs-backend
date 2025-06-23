# ByteLogs Forum Backend

ByteLogs 是一个现代化的论坛系统后端，采用 Spring Boot 3.5.0 和 JDK 21 构建，提供完整的用户管理、文章发布、分类管理等论坛核心功能。

## 项目特色

- 基于 Spring Boot 3.5.0 和 JDK 21，性能卓越
- 清晰的多模块架构，代码组织有序
- 完整的 JWT 认证和权限控制体系
- 支持 Markdown 文章编辑和展示
- RESTful API 设计，前后端分离
- 完整的 API 文档（SpringDoc OpenAPI 3）
- MapStruct 自动映射，开发效率高

## 项目架构

项目采用清晰的四层多模块架构：

```
bytelogs-backend/
├── bytelogs-api/        # API 层：DTO、VO、枚举、请求响应模型
├── bytelogs-core/       # 核心层：工具类、配置、安全、上下文管理
├── bytelogs-service/    # 服务层：业务逻辑、数据访问、实体映射
└── bytelogs-web/        # Web 层：控制器、过滤器、安全配置
```

**依赖关系**：bytelogs-web → bytelogs-service → bytelogs-core ← bytelogs-api

### 核心模块说明

- **bytelogs-api**: 包含所有的 DTO、VO、枚举定义和 API 接口契约
- **bytelogs-core**: 提供基础设施组件，包括工具类、配置、JWT 处理、异常处理等
- **bytelogs-service**: 实现核心业务逻辑，包含 DAO、Service、实体类和 MapStruct 映射器
- **bytelogs-web**: Web 层实现，包含 REST 控制器、过滤器和 Spring Security 配置

## 技术栈

### 核心框架
- **Spring Boot 3.5.0** - 应用框架
- **JDK 21** - Java 运行环境
- **MyBatis-Plus 3.5.12** - ORM 框架，支持自动 CRUD
- **Spring Security** - 安全框架

### 数据存储
- **MySQL 8.0+** - 主数据库
- **Redis** - 缓存和会话存储

### 工具库
- **MapStruct 1.5.5** - 对象映射框架
- **JWT (jjwt 0.11.5)** - 身份认证
- **Flexmark 0.62.2** - Markdown 处理
- **SpringDoc OpenAPI 3** - API 文档生成
- **Apache Commons Lang3** - 通用工具类
- **Guava** - Google 工具库

## 核心功能

### 用户管理
- 用户注册、登录、JWT 认证
- 用户信息管理和权限控制
- 管理员权限体系

### 文章系统
- 文章创建、编辑、发布
- Markdown 支持
- 文章分类和标签管理
- 文章状态管理（草稿、发布、审核）

### 分类标签
- 分类层级管理
- 标签系统
- 文章分类关联

### 权限控制
- 基于注解的权限控制（`@RequiresLogin`、`@RequiresAdmin`）
- JWT Token 认证
- 请求上下文管理

## 开发环境要求

- **JDK 21+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Redis 6+**

## 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/yourusername/bytelogs-backend.git
cd bytelogs-backend
```

### 2. 数据库配置
创建 MySQL 数据库 `byte_logs`，执行项目根目录下的 `schema-byteilogs-v1.0.sql` 初始化数据库。

### 3. 配置文件
根据你的环境修改配置文件：
- 开发环境：`bytelogs-web/src/main/resources-env/dev/application-dal.yml`
- 生产环境：`bytelogs-web/src/main/resources-env/prod/application-dal.yml`

### 4. 构建运行
```bash
# 构建项目（跳过测试）
mvn clean install -DskipTests

# 运行应用
java -jar bytelogs-web/target/bytelogs-web-0.0.1-SNAPSHOT.jar

# 或者通过 IDE 运行主类：top.harrylei.forum.web.QuickWebApplication
```

### 5. 访问应用
- 应用地址：http://localhost:8080
- API 文档：http://localhost:8080/swagger-ui.html

## 开发指南

### 代码规范
- 实体类：`*DO` (如 `ArticleDO`)
- DTO 类：`*DTO` (如 `ArticleDTO`)
- VO 类：`*VO` (如 `ArticleVO`)
- 请求类：`*Req` (如 `ArticlePostReq`)
- 服务接口：`*Service`，实现类：`*ServiceImpl`
- DAO 类：`*DAO`
- Mapper 接口：`*Mapper`

### 添加新功能
1. 在 `bytelogs-service` 创建实体类和 DAO
2. 在 `bytelogs-api` 创建 DTO/VO 和请求类
3. 使用 MapStruct 创建映射器
4. 实现服务层业务逻辑
5. 在 `bytelogs-web` 创建控制器

### 测试
```bash
# 运行所有测试
mvn test

# 运行特定模块测试
mvn test -pl bytelogs-service
```

## 部署说明

### 环境配置
- 使用 `dev` profile 进行开发
- 使用 `prod` profile 进行生产部署

### 配置项
- JWT 密钥和有效期
- 数据库连接信息
- Redis 连接配置
- 日志级别配置

## API 文档

启动应用后，访问 http://localhost:8080/swagger-ui.html 查看完整的 API 文档。

主要 API 端点：
- `/api/auth/*` - 认证相关
- `/api/article/*` - 文章管理
- `/api/category/*` - 分类管理
- `/api/tag/*` - 标签管理
- `/api/user/*` - 用户管理
- `/api/admin/*` - 管理员功能

## 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。
