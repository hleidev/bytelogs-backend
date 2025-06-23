# 数据库脚本说明

本目录包含 ByteLogs 论坛系统的数据库相关脚本和文档。

## 文件说明

- `init-schema.sql` - 数据库初始化脚本，包含所有表结构定义
- `sample-data.sql` - 示例数据脚本（可选）
- `migration/` - 数据库迁移脚本目录

## 使用方法

### 初始化数据库

1. 确保 MySQL 8.0+ 已安装并运行
2. 执行初始化脚本：

```bash
# 方式一：通过 MySQL 命令行
mysql -u root -p < docs/database/init-schema.sql

# 方式二：通过 MySQL 客户端
mysql -u root -p
source docs/database/init-schema.sql;
```

### 数据库配置

初始化完成后，数据库 `byte_logs` 将包含以下核心表：

- `user_account` - 用户账号表
- `user_info` - 用户信息表  
- `article` - 文章表
- `article_detail` - 文章详情表
- `category` - 分类表
- `tag` - 标签表
- `article_tag` - 文章标签关联表
- `comment` - 评论表

### 环境配置

修改应用配置文件中的数据库连接信息：

```yaml
# bytelogs-web/src/main/resources-env/dev/application-dal.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/byte_logs?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

## 注意事项

- 执行脚本前请备份现有数据
- 确保 MySQL 字符集设置为 `utf8mb4`
- 生产环境部署前请仔细检查配置