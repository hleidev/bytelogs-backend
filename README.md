# ByteLogs 社区后端

基于 Spring Boot 3.5.0 + JDK 21 的现代化技术社区系统，采用事件驱动架构，支持用户管理、文章发布、评论系统、活动排行和通知等功能。

## 项目架构

采用清晰的四层多模块架构：

```
bytelogs-backend/
├── bytelogs-api/        # DTOs, VOs, 枚举, 请求响应模型
├── bytelogs-core/       # 工具类, 配置, 安全, 上下文管理
├── bytelogs-service/    # 业务逻辑, 数据访问, 实体映射
└── bytelogs-web/        # 控制器, 过滤器, 安全配置
```

**依赖关系**: web → service → core → api

## 技术栈

- **Spring Boot 3.5.0** + JDK 21
- **MySQL 8.0+** + Redis + Kafka
- **MyBatis-Plus** + Spring Security + JWT
- **MapStruct** + SpringDoc OpenAPI 3

## 核心功能

### 用户系统
- 用户注册/登录、JWT认证、权限控制
- 用户关注功能、关注列表和粉丝列表

### 文章系统
- 文章创建/编辑/发布、分类和标签管理
- 文章版本管理和回滚、点赞收藏功能

### 评论系统
- 评论发表和回复、点赞功能

### AI 智能聊天
- 支持多种AI服务（DeepSeek、通义千问）
- 智能对话、上下文记忆、会话管理

### 活跃度系统
- 用户行为积分统计（点赞、评论、发文等）
- 实时排行榜（日榜/月榜/总榜）
- 基于Redis的高性能积分计算

### 通知系统
- 实时消息通知（点赞、评论、关注等）
- Kafka异步消息处理

### 系统特性
- Redis健康监控、防重复提交机制
- 事件驱动架构、RESTful API设计

## 环境要求

- **JDK 21+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Redis 6+**
- **Kafka 2.8+**

## 快速开始

### 1. 数据库初始化
```bash
mysql -u root -p < docs/database/init-schema.sql
```

### 2. 配置文件
复制示例配置文件并修改数据库密码：
```bash
cp bytelogs-web/src/main/resources-env/dev/application-dal.yml.example \
   bytelogs-web/src/main/resources-env/dev/application-dal.yml
```

### 3. 构建运行
```bash
# 构建项目
mvn clean install -DskipTests

# 运行应用
java -jar bytelogs-web/target/bytelogs-web-0.0.1-SNAPSHOT.jar
```

### 4. 访问应用
- 应用地址：http://localhost:8080
- API文档：http://localhost:8080/swagger-ui.html

## API 接口

主要接口模块：
- `/api/v1/auth/*` - 认证相关
- `/api/v1/article/*` - 文章管理
- `/api/v1/user/*` - 用户管理
- `/api/v1/comment/*` - 评论管理
- `/api/v1/ai/*` - AI聊天功能
- `/api/v1/activity/*` - 活跃度排行榜
- `/api/v1/notify/*` - 通知消息
- `/api/v1/admin/*` - 管理员功能

详细文档：http://localhost:8080/swagger-ui.html