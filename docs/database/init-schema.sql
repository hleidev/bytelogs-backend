DROP DATABASE IF EXISTS `byte_logs`;
CREATE DATABASE `byte_logs` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `byte_logs`;

-- 用户账号表
CREATE TABLE `user_account`
(
    `id`               bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `third_account_id` varchar(128)    NOT NULL DEFAULT '' COMMENT '第三方账号ID',
    `user_name`        varchar(64)     NOT NULL DEFAULT '' COMMENT '用户名',
    `password`         char(60)        NOT NULL DEFAULT '' COMMENT '密码,BCrypt加密',
    `login_type`       tinyint         NOT NULL DEFAULT 0 COMMENT '登录类型,0:密码登录,1:邮箱验证码登录',
    `email`            varchar(128)             DEFAULT '' COMMENT '邮箱',
    `status`           tinyint         NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用，1-启用',
    `deleted`          tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除,0:未删除,1:已删除',
    `create_time`      timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_account_name_type` (`user_name`, `login_type`) COMMENT '用户名和登录类型唯一约束'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '用户账号表';

-- 用户信息表
CREATE TABLE `user_info`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     bigint unsigned NOT NULL DEFAULT 0 COMMENT '用户ID',
    `user_name`   varchar(64)     NOT NULL DEFAULT '' COMMENT '用户名',
    `avatar`      varchar(256)    NOT NULL DEFAULT '' COMMENT '用户头像',
    `position`    varchar(64)     NOT NULL DEFAULT '' COMMENT '职位',
    `company`     varchar(64)     NOT NULL DEFAULT '' COMMENT '公司',
    `profile`     varchar(500)    NOT NULL DEFAULT '' COMMENT '个人简介',
    `email`       varchar(128)    NOT NULL DEFAULT '' COMMENT '用户邮箱',
    `user_role`   tinyint         NOT NULL DEFAULT 0 COMMENT '用户角色,0:普通用户,1:管理员',
    `extend`      varchar(1024)   NOT NULL DEFAULT '' COMMENT '扩展信息,JSON格式',
    `deleted`     tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除,0:未删除,1:已删除',
    `create_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`) COMMENT '用户ID唯一约束'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '用户信息表';

-- 用户关注关系表
CREATE TABLE `user_relation`
(
    `id`             bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        bigint unsigned NOT NULL DEFAULT 0 COMMENT '发起关注的用户ID',
    `follow_user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '被关注的用户ID',
    `follow_state`   tinyint         NOT NULL DEFAULT 1 COMMENT '关注状态：0-未关注，1-已关注，2-取消关注',
    `deleted`        tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除：0-正常，1-已删除（逻辑删除）',
    `create_time`    timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`) COMMENT '用户关注唯一约束',
    KEY `idx_follow_user_id` (`follow_user_id`) COMMENT '被关注用户索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '用户关注关系表';

-- 用户内容行为足迹表
CREATE TABLE `user_foot`
(
    `id`               bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`          bigint unsigned NOT NULL DEFAULT 0 COMMENT '用户ID',
    `content_id`       bigint unsigned NOT NULL DEFAULT 0 COMMENT '内容ID（文章或评论）',
    `content_type`     tinyint         NOT NULL DEFAULT 1 COMMENT '内容类型：1-文章，2-评论',
    `content_user_id`  bigint unsigned NOT NULL DEFAULT 0 COMMENT '该内容的作者用户ID',
    `collection_state` tinyint         NOT NULL DEFAULT 0 COMMENT '收藏状态：0-未收藏，1-已收藏，2-取消收藏',
    `read_state`       tinyint         NOT NULL DEFAULT 0 COMMENT '阅读状态：0-未读，1-已读',
    `comment_state`    tinyint         NOT NULL DEFAULT 0 COMMENT '评论状态：0-未评论，1-已评论，2-已删评论',
    `praise_state`     tinyint         NOT NULL DEFAULT 0 COMMENT '点赞状态：0-未点赞，1-已点赞，2-取消点赞',
    `deleted`          tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除：0-正常，1-已删除（逻辑删除）',
    `create_time`      timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_content` (`user_id`, `content_id`, `content_type`) COMMENT '用户内容行为唯一约束',
    KEY `idx_content_id` (`content_id`) COMMENT '内容ID索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '用户内容行为足迹表';

-- 类目管理表
CREATE TABLE `category`
(
    `id`            bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_name` varchar(64)     NOT NULL DEFAULT '' COMMENT '类目名称',
    `status`        tinyint         NOT NULL DEFAULT 0 COMMENT '状态：0-未发布，1-已发布',
    `sort`          int             NOT NULL DEFAULT 0 COMMENT '排序值（越大越靠前）',
    `deleted`       tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time`   timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_name` (`category_name`, `deleted`) COMMENT '类目名称唯一约束'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '类目管理表';

-- 标签管理表
CREATE TABLE `tag`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tag_name`    varchar(64)     NOT NULL COMMENT '标签名称',
    `tag_type`    tinyint         NOT NULL DEFAULT 1 COMMENT '标签类型：1-系统标签，2-自定义标签',
    `category_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '类目ID',
    `status`      tinyint         NOT NULL DEFAULT 0 COMMENT '状态：0-未发布，1-已发布',
    `deleted`     tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_name_category` (`tag_name`, `category_id`, `deleted`) COMMENT '标签名称类目唯一约束',
    KEY `idx_category_id` (`category_id`) COMMENT '类目ID索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '标签管理表';

-- 文章表
CREATE TABLE `article`
(
    `id`                bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`           bigint unsigned NOT NULL DEFAULT 0 COMMENT '用户ID',
    `article_type`      tinyint         NOT NULL DEFAULT 1 COMMENT '文章类型：1-博文，2-问答',
    `title`             varchar(200)    NOT NULL DEFAULT '' COMMENT '文章标题',
    `short_title`       varchar(200)    NOT NULL DEFAULT '' COMMENT '短标题',
    `picture`           varchar(512)    NOT NULL DEFAULT '' COMMENT '文章头图',
    `summary`           varchar(512)    NOT NULL DEFAULT '' COMMENT '文章摘要',
    `category_id`       bigint unsigned NOT NULL DEFAULT 0 COMMENT '类目ID',
    `source`            tinyint         NOT NULL DEFAULT 1 COMMENT '来源：1-转载，2-原创，3-翻译',
    `source_url`        varchar(512)    NOT NULL DEFAULT '' COMMENT '原文链接',
    `official`          tinyint         NOT NULL DEFAULT 0 COMMENT '官方状态：0-非官方，1-官方',
    `topping`           tinyint         NOT NULL DEFAULT 0 COMMENT '置顶状态：0-不置顶，1-置顶',
    `cream`             tinyint         NOT NULL DEFAULT 0 COMMENT '加精状态：0-不加精，1-加精',
    `status`            tinyint         NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-待审核',
    `current_version`   int unsigned    NOT NULL DEFAULT 1 COMMENT '当前最大版本号（用于生成新版本）',
    `published_version` int unsigned    NOT NULL DEFAULT 0 COMMENT '已发布版本号（0表示未发布）',
    `deleted`           tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time`       timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`) COMMENT '类目ID索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '文章表';

-- 文章详情表
CREATE TABLE `article_detail`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`  bigint unsigned NOT NULL DEFAULT 0 COMMENT '文章ID',
    `version`     int unsigned    NOT NULL DEFAULT 1 COMMENT '版本号',
    `content`     longtext COMMENT '文章内容',
    `edit_token`  varchar(36)     NOT NULL DEFAULT '' COMMENT '编辑操作令牌（防止并发编辑）',
    `deleted`     tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_version` (`article_id`, `version`) COMMENT '文章版本唯一约束'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '文章详情表';

-- 文章标签映射表
CREATE TABLE `article_tag`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`  bigint unsigned NOT NULL DEFAULT 0 COMMENT '文章ID',
    `tag_id`      bigint unsigned NOT NULL DEFAULT 0 COMMENT '标签ID',
    `deleted`     tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`) COMMENT '文章标签唯一约束',
    KEY `idx_tag_id` (`tag_id`) COMMENT '标签ID索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '文章标签映射表';

-- 评论表
CREATE TABLE `comment`
(
    `id`                bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`        bigint unsigned NOT NULL DEFAULT 0 COMMENT '评论所属文章ID',
    `user_id`           bigint unsigned NOT NULL DEFAULT 0 COMMENT '评论用户ID',
    `content`           varchar(500)    NOT NULL DEFAULT '' COMMENT '评论内容',
    `top_comment_id`    bigint unsigned          DEFAULT 0 COMMENT '顶级评论ID,如果是一级评论则为0',
    `parent_comment_id` bigint unsigned          DEFAULT 0 COMMENT '父评论ID,如果是一级评论则为0',
    `deleted`           tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除,0:未删除,1:已删除',
    `create_time`       timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`) COMMENT '文章ID索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '评论表';

-- 消息通知表
CREATE TABLE `notify_msg`
(
    `id`              bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `related_id`      bigint unsigned NOT NULL DEFAULT 0 COMMENT '关联的主键',
    `notify_user_id`  bigint unsigned NOT NULL DEFAULT 0 COMMENT '通知的用户ID',
    `operate_user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '触发通知的用户ID',
    `msg`             varchar(1024)   NOT NULL DEFAULT '' COMMENT '消息内容',
    `type`            tinyint         NOT NULL DEFAULT 0 COMMENT '类型: 0-默认，1-评论，2-回复，3-点赞，4-收藏，5-关注，6-系统',
    `state`           tinyint         NOT NULL DEFAULT 0 COMMENT '阅读状态: 0-未读，1-已读',
    `create_time`     timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '消息通知表';

-- 内容访问计数表
CREATE TABLE `read_count`
(
    `id`           bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `content_id`   bigint unsigned NOT NULL DEFAULT 0 COMMENT '内容ID（文章或评论）',
    `content_type` tinyint         NOT NULL DEFAULT 1 COMMENT '内容类型：1-文章，2-评论',
    `cnt`          int unsigned    NOT NULL DEFAULT 0 COMMENT '访问计数',
    `create_time`  timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_content_id_type` (`content_id`, `content_type`) COMMENT '内容类型唯一约束'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '内容访问计数表';

-- 请求计数表
CREATE TABLE `request_count`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `host`        varchar(64)     NOT NULL DEFAULT '' COMMENT '机器IP地址',
    `cnt`         int unsigned    NOT NULL DEFAULT 0 COMMENT '访问计数',
    `date`        date            NOT NULL COMMENT '计数日期',
    `create_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_date_host` (`date`, `host`) COMMENT '日期主机唯一约束'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '请求计数表';
