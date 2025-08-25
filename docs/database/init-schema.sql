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
    UNIQUE KEY `uk_user_name` (`user_name`) COMMENT '用户名唯一约束'
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
    `tag_type`    tinyint         NOT NULL DEFAULT 1 COMMENT '标签类型：1-系统标签，2-用户标签',
    `creator_id`  bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者ID（0-系统标签）',
    `deleted`     tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_name` (`tag_name`) COMMENT '标签名称唯一约束',
    KEY `idx_creator_id` (`creator_id`) COMMENT '创建者ID索引',
    KEY `idx_tag_type` (`tag_type`) COMMENT '标签类型索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '标签管理表';

-- 文章表
CREATE TABLE `article`
(
    `id`            bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       bigint unsigned NOT NULL DEFAULT 0 COMMENT '用户ID',
    `article_type`  tinyint         NOT NULL DEFAULT 1 COMMENT '文章类型：1-博文，2-问答',
    `official`      tinyint         NOT NULL DEFAULT 0 COMMENT '官方状态：0-非官方，1-官方',
    `topping`       tinyint         NOT NULL DEFAULT 0 COMMENT '置顶状态：0-不置顶，1-置顶',
    `cream`         tinyint         NOT NULL DEFAULT 0 COMMENT '加精状态：0-不加精，1-加精',
    `version_count` int unsigned    NOT NULL DEFAULT 0 COMMENT '版本总数',
    `deleted`       tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time`   timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '文章基础表';

-- 文章详细表
CREATE TABLE `article_detail`
(
    `id`           bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`   bigint unsigned NOT NULL DEFAULT 0 COMMENT '文章ID',
    `version`      int unsigned    NOT NULL DEFAULT 1 COMMENT '版本号',
    `title`        varchar(200)    NOT NULL DEFAULT '' COMMENT '文章标题',
    `short_title`  varchar(200)    NOT NULL DEFAULT '' COMMENT '短标题',
    `picture`      varchar(512)    NOT NULL DEFAULT '' COMMENT '文章头图',
    `summary`      varchar(512)    NOT NULL DEFAULT '' COMMENT '文章摘要',
    `category_id`  bigint unsigned NOT NULL DEFAULT 0 COMMENT '类目ID',
    `source`       tinyint         NOT NULL DEFAULT 1 COMMENT '来源：1-转载，2-原创，3-翻译',
    `source_url`   varchar(512)    NOT NULL DEFAULT '' COMMENT '原文链接',
    `content`      longtext        NOT NULL COMMENT '文章内容',
    `status`       tinyint         NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-待审核，3-审核拒绝',
    `latest`       tinyint         NOT NULL DEFAULT 0 COMMENT '最新版本标记：0-否，1-是',
    `published`    tinyint         NOT NULL DEFAULT 0 COMMENT '发布版本标记：0-否，1-是',
    `publish_time` timestamp       NULL     DEFAULT NULL COMMENT '发布时间',
    `deleted`      tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time`  timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_version` (`article_id`, `version`) COMMENT '文章版本唯一索引',
    KEY `idx_article_latest` (`article_id`, `latest`) COMMENT '文章最新版本索引',
    KEY `idx_article_published` (`article_id`, `published`) COMMENT '文章发布版本索引',
    KEY `idx_status` (`status`) COMMENT '状态索引',
    KEY `idx_category` (`category_id`) COMMENT '类目索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '文章详细表';

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
    `content_type`    tinyint         NOT NULL DEFAULT 0 COMMENT '内容类型: 0-不适用，1-文章，2-评论',
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

-- 活跃度排行榜表
CREATE TABLE `activity_rank`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     bigint unsigned NOT NULL DEFAULT 0 COMMENT '用户ID',
    `rank_type`   tinyint         NOT NULL DEFAULT 0 COMMENT '排行榜类型: 1-总榜,2-月榜,3-日榜',
    `rank_period` varchar(10)     NOT NULL DEFAULT '' COMMENT '排行榜周期: 日榜2025-01-15, 月榜2025-01, 总榜total',
    `score`       int unsigned    NOT NULL DEFAULT 0 COMMENT '积分',
    `rank`        int unsigned    NOT NULL DEFAULT 0 COMMENT '排名位置',
    `deleted`     tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除,0:未删除,1:已删除',
    `create_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_type_period` (`user_id`, `rank_type`, `rank_period`) COMMENT '用户排行榜唯一约束',
    KEY `idx_type_period_score` (`rank_type`, `rank_period`, `score` DESC) COMMENT '排行榜查询索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '用户活跃度排行榜表';

-- AI对话表
CREATE TABLE `ai_conversation`
(
    `id`                   bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '对话ID',
    `user_id`              bigint unsigned NOT NULL DEFAULT 0 COMMENT '用户ID',
    `title`                varchar(80)     NOT NULL DEFAULT '' COMMENT '对话标题',
    `status`               tinyint         NOT NULL DEFAULT 1 COMMENT '对话状态：1-进行中，2-已归档',
    `message_count`        int             NOT NULL DEFAULT 0 COMMENT '消息数量',
    `last_message_time`    timestamp                DEFAULT NULL COMMENT '最后消息时间',
    `last_message_preview` varchar(200)    NOT NULL DEFAULT '' COMMENT '最后消息内容预览',
    `deleted`              tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time`          timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_status` (`user_id`, `status`, `deleted`),
    KEY `idx_last_message_time` (`last_message_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = 'AI对话表';

-- AI消息表
CREATE TABLE `ai_message`
(
    `id`              bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `conversation_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '对话ID',
    `user_id`         bigint unsigned NOT NULL DEFAULT 0 COMMENT '用户ID',
    `role`            tinyint         NOT NULL DEFAULT 1 COMMENT '消息角色：1-用户，2-AI助手，3-系统',
    `content`         longtext        NOT NULL COMMENT '消息内容',
    `vendor`          tinyint         NOT NULL DEFAULT 1 COMMENT '使用的AI厂商类型：1-DeepSeek，2-通义千问',
    `model`           varchar(50)     NOT NULL DEFAULT '' COMMENT '具体模型名称（如：qwen-turbo、deepseek-chat）',
    `input_tokens`    int             NOT NULL DEFAULT 0 COMMENT '输入Token消耗（用户消息和上下文）',
    `output_tokens`   int             NOT NULL DEFAULT 0 COMMENT 'AI生成Token消耗',
    `total_tokens`    int             NOT NULL DEFAULT 0 COMMENT '总Token消耗',
    `deleted`         tinyint         NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time`     timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_conversation_time` (`conversation_id`, `create_time`),
    KEY `idx_vendor_model` (`vendor`, `model`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = 'AI消息表';

-- AI使用统计表
CREATE TABLE `ai_usage_stats`
(
    `id`                 bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `user_id`            bigint unsigned NOT NULL DEFAULT 0 COMMENT '用户ID',
    `date`               date            NOT NULL COMMENT '统计日期',
    `message_count`      int             NOT NULL DEFAULT 0 COMMENT '消息数量',
    `tokens_used`        int             NOT NULL DEFAULT 0 COMMENT 'Token消耗总量',
    `conversation_count` int             NOT NULL DEFAULT 0 COMMENT '对话数量',
    `create_time`        timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `date`),
    KEY `idx_date` (`date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = 'AI使用统计表';