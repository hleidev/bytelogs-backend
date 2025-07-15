# ByteLogs 论坛后端

ByteLogs 是一个现代化的论坛系统后端，采用 Spring Boot 3.5.0 和 JDK 21 构建，提供完整的用户管理、文章发布、分类管理等论坛核心功能。

## 项目特色

- 基于 Spring Boot 3.5.0 和 JDK 21，性能卓越
- 清晰的多模块架构，代码组织有序
- 完整的 JWT 认证和权限控制体系
- 文章编辑和展示功能
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

**依赖关系**：bytelogs-web → bytelogs-service → bytelogs-core → bytelogs-api

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
- **SpringDoc OpenAPI 3** - API 文档生成

## 核心功能

### 用户管理
- 用户注册、登录、JWT 认证
- 用户信息管理和权限控制
- 用户关注功能，支持关注/取消关注
- 关注列表和粉丝列表查询
- 管理员权限体系

### 文章系统
- 文章创建、编辑、发布
- 文章分类和标签管理
- 文章状态管理（草稿、发布、审核）
- 文章版本回滚功能
- 文章点赞、收藏功能

### 评论系统
- 评论发表和回复
- 评论点赞、收藏功能
- 评论管理和审核

### 权限控制
- 基于注解的权限控制（`@RequiresLogin`、`@RequiresAdmin`）
- JWT Token 认证
- 请求上下文管理

### 防重复提交机制
- 基于 Redis 的分布式锁防重复提交
- 支持用户关注、文章点赞、评论点赞等操作
- 可配置的防重复提交时间窗口

## 环境要求

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
创建 MySQL 数据库，执行数据库初始化脚本

### 3. 配置文件
复制示例配置文件并修改为你的环境配置：
```bash
# 复制数据库配置文件
cp bytelogs-web/src/main/resources-env/dev/application-dal.yml.example \
   bytelogs-web/src/main/resources-env/dev/application-dal.yml

# 复制消息队列配置文件
cp bytelogs-web/src/main/resources-env/dev/application-mq.yml.example \
   bytelogs-web/src/main/resources-env/dev/application-mq.yml
```

然后修改配置文件中的数据库和Redis密码：
- 开发环境：`bytelogs-web/src/main/resources-env/dev/application-dal.yml`

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

## API 文档

启动应用后，访问 http://localhost:8080/swagger-ui.html 查看完整的 API 文档。

主要 API 端点：
- `/api/v1/auth/*` - 认证相关
- `/api/v1/article/*` - 文章管理
- `/api/v1/category/*` - 分类管理
- `/api/v1/tag/*` - 标签管理
- `/api/v1/user/*` - 用户管理
- `/api/v1/comment/*` - 评论管理
- `/api/v1/admin/*` - 管理员功能

## 项目状态

此项目目前处于开发阶段，正在持续改进和功能完善中。
