# ByteLogs Forum Backend

本项目是一个简单的论坛系统后端，基于 Spring Boot 3.2.3 构建。

## 项目结构

项目采用多模块结构，包含以下模块：

- `bytelogs-api`: 包含 DTO、VO、异常定义等通用结构
- `bytelogs-core`: 包含核心工具类、配置、中间件封装等基础设施

## 技术栈

- Spring Boot 3.2.3
- JDK 17
- MyBatis-Plus 3.5.2
- Redis
- MySQL 8.0+
- Knife4j & OpenAPI 3
- MapStruct 1.5.5.Final

## 从 Spring Boot 2.7.1 升级到 3.2.3

本项目已从 Spring Boot 2.7.1 升级到 Spring Boot 3.2.3，以下是主要变更：

### 核心依赖变更

1. 将 Spring Boot 父 POM 版本从 2.7.1 升级到 3.2.3
2. 将 `javax.servlet-api` 替换为 `jakarta.servlet-api`
3. 将 `javax.annotation` 替换为 `jakarta.annotation`
4. 将 `mysql-connector-java` 替换为 `mysql-connector-j`
5. 将 Knife4j 从 `knife4j-openapi2` 升级到 `knife4j-openapi3`

### API 变更

1. Swagger 注解替换：
   - `@ApiModel` → `@Schema`
   - `@ApiModelProperty` → `@Schema`

### 移除的功能

为了精简项目结构，以下功能被禁用或移除：

1. RabbitMQ 集成
2. 敏感词过滤
3. 邮件服务
4. 二维码生成
5. IP 地区解析
6. WebSocket 支持
7. Druid 数据源监控
8. 异步执行框架
9. MDC 追踪
10. Redis 客户端封装

## 升级优势

1. **长期支持**：Spring Boot 3.x 将获得更长时间的支持
2. **性能提升**：基于 Spring Framework 6 的性能优化
3. **新特性**：原生镜像支持、可观测性改进
4. **更好的 Java 17 支持**：更好地利用 Java 17 特性

## 开发环境

本项目需要以下环境：

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6+

## 构建与运行

```bash
# 克隆项目
git clone https://github.com/yourusername/bytelogs-backend.git

# 进入项目目录
cd bytelogs-backend

# 编译项目
mvn clean install -DskipTests

# 运行项目
java -jar bytelogs-core/target/bytelogs-core-0.0.1-SNAPSHOT.jar
```
