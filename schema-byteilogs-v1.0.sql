DROP DATABASE IF EXISTS `byte_logs`;
CREATE DATABASE `byte_logs` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `byte_logs`;

create table `user_account`
(
    id               bigint unsigned not null auto_increment comment '主键ID',
    third_account_id varchar(128)    not null default '' comment '第三方账号ID',
    user_name        varchar(16)     NOT NULL DEFAULT '' COMMENT '用户名',
    password         char(64)        not null default '' comment '密码,BCrypt加密',
    login_type       tinyint         not null default 0 comment '登录类型,0:密码登录,1:邮箱验证码登录',
    email            varchar(128)             default '' comment '邮箱',
    status           TINYINT         NOT NULL DEFAULT 1 comment '账号状态：0-禁用，1-启用',
    deleted          tinyint         not null default 0 comment '是否删除,0:未删除,1:已删除',
    create_time      timestamp       not null default current_timestamp comment '创建时间',
    update_time      timestamp       not null default current_timestamp on update current_timestamp comment '更新时间',
    primary key (id),
    unique key uk_user_account_name_type (user_name, login_type) comment '用户名和登录类型唯一约束',
    key idx_third_account_id (third_account_id) comment '第三方账号ID索引'
) engine = InnoDB
  Default charset = utf8mb4 comment '用户账号表';

create table `comment`
(
    id                bigint unsigned not null auto_increment comment '主键ID',
    article_id        bigint unsigned not null default 0 comment '评论所属文章ID',
    user_id           bigint unsigned not null default 0 comment '评论用户ID',
    content           varchar(300)    not null default '' comment '评论内容',
    top_comment_id    bigint unsigned          default 0 comment '顶级评论ID,如果是一级评论则为0',
    parent_comment_id bigint unsigned          default 0 comment '父评论ID,如果是一级评论则为0',
    deleted           tinyint         not null default 0 comment '是否删除,0:未删除,1:已删除',
    create_time       timestamp       not null default current_timestamp comment '创建时间',
    update_time       timestamp       not null default current_timestamp on update current_timestamp comment '更新时间',
    primary key (id),
    key idx_article_id (article_id) comment '文章ID索引',
    key idx_user_id (user_id) comment '用户ID索引'
) engine = InnoDB
  Default charset = utf8mb4 comment '评论表';

create table user_info
(
    id          bigint unsigned not null auto_increment comment '主键ID',
    user_id     bigint unsigned not null default 0 comment '用户ID',
    user_name   varchar(64)     not null default '' comment '用户名',
    photo       varchar(128)    not null default '' comment '用户图像',
    position    varchar(64)     not null default '' comment '职位',
    company     varchar(64)     not null default '' comment '公司',
    profile     varchar(255)    not null default '' comment '个人简介',
    email       varchar(128)    not null default '' COMMENT '用户邮箱',
    user_role   tinyint         not null default 0 comment '用户角色,0:普通用户,1:管理员',
    extend      varchar(1024)   not null default '' comment '扩展信息,JSON格式',
    deleted     tinyint         not null default 0 comment '是否删除,0:未删除,1:已删除',
    create_time timestamp       not null default current_timestamp comment '创建时间',
    update_time timestamp       not null default current_timestamp on update current_timestamp comment '更新时间',
    primary key (id),
    key idx_user_name (user_id) comment '用户名索引',
    key idx_user_id (user_id) comment '用户ID索引'
) engine = InnoDB
  Default charset = utf8mb4 comment '用户信息表';

CREATE TABLE `user_relation`
(
    `id`             bigint unsigned  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        bigint unsigned  NOT NULL DEFAULT 0 COMMENT '发起关注的用户ID',
    `follow_user_id` bigint unsigned  NOT NULL DEFAULT 0 COMMENT '被关注的用户ID',
    `follow_state`   tinyint unsigned NOT NULL DEFAULT 1 COMMENT '关注状态：0-未关注，1-已关注，2-取消关注',
    `deleted`        tinyint          NOT NULL DEFAULT 0 COMMENT '是否删除：0-正常，1-已删除（逻辑删除）',
    `create_time`    timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`) COMMENT '唯一索引，防止重复关注',
    KEY `idx_follow_user_id` (`follow_user_id`) COMMENT '被关注用户索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '用户关注关系表';

CREATE TABLE `user_foot`
(
    `id`               bigint unsigned  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '用户ID',
    `content_id`       bigint unsigned  NOT NULL DEFAULT 0 COMMENT '内容ID（文章或评论）',
    `content_type`     tinyint unsigned NOT NULL DEFAULT 1 COMMENT '内容类型：1-文章，2-评论',
    `content_user_id`  bigint unsigned  NOT NULL DEFAULT 0 COMMENT '该内容的作者用户ID',
    `collection_state` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '收藏状态：0-未收藏，1-已收藏，2-取消收藏',
    `read_state`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '阅读状态：0-未读，1-已读',
    `comment_state`    tinyint unsigned NOT NULL DEFAULT 0 COMMENT '评论状态：0-未评论，1-已评论，2-已删评论',
    `praise_state`     tinyint unsigned NOT NULL DEFAULT 0 COMMENT '点赞状态：0-未点赞，1-已点赞，2-取消点赞',
    `deleted`          tinyint          NOT NULL DEFAULT 0 COMMENT '是否删除：0-正常，1-已删除（逻辑删除）',
    `create_time`      timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_content` (`user_id`, `content_id`, `content_type`) COMMENT '用户内容行为唯一约束',
    KEY `idx_content_id` (`content_id`) COMMENT '内容ID索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '用户内容行为足迹表';

CREATE TABLE `article`
(
    `id`              bigint unsigned  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         bigint unsigned  NOT NULL DEFAULT 0 COMMENT '用户ID',
    `article_type`    tinyint unsigned NOT NULL DEFAULT 1 COMMENT '文章类型：1-博文，2-问答',
    `title`           varchar(120)     NOT NULL DEFAULT '' COMMENT '文章标题',
    `short_title`     varchar(120)     NOT NULL DEFAULT '' COMMENT '短标题',
    `picture`         varchar(128)     NOT NULL DEFAULT '' COMMENT '文章头图',
    `summary`         varchar(300)     NOT NULL DEFAULT '' COMMENT '文章摘要',
    `category_id`     bigint unsigned  NOT NULL DEFAULT 0 COMMENT '类目ID',
    `source`          tinyint unsigned NOT NULL DEFAULT 1 COMMENT '来源：1-转载，2-原创，3-翻译',
    `source_url`      varchar(128)     NOT NULL DEFAULT '' COMMENT '原文链接',
    `offical_stat`    bigint unsigned  NOT NULL DEFAULT 0 COMMENT '官方状态：0-非官方，1-官方',
    `topping_stat`    bigint unsigned  NOT NULL DEFAULT 0 COMMENT '置顶状态：0-不置顶，1-置顶',
    `cream_stat`      bigint unsigned  NOT NULL DEFAULT 0 COMMENT '加精状态：0-不加精，1-加精',
    `status`          tinyint unsigned NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-待审核，2-已发布，3-下架，4-驳回',
    `current_version` int unsigned     NOT NULL DEFAULT 1 COMMENT '当前发布版本号',
    `deleted`         tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time`     timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_title` (`title`),
    KEY `idx_short_title` (`short_title`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文章表';

CREATE TABLE `article_detail`
(
    `id`           bigint unsigned  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`   bigint unsigned  NOT NULL DEFAULT 0 COMMENT '文章ID',
    `version`      int unsigned     NOT NULL DEFAULT 1 COMMENT '版本号',
    `content`      longtext COMMENT '文章内容',
    `is_latest`    tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否为最新版本',
    `is_published` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否为发布版本',
    `edit_token`   varchar(36)      NOT NULL DEFAULT '' COMMENT '编辑操作令牌（防止并发编辑）',
    `deleted`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time`  timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_article_version` (`article_id`, `version`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文章详情表';

CREATE TABLE `category`
(
    `id`            bigint unsigned  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_name` varchar(16)      NOT NULL DEFAULT '' COMMENT '类目名称',
    `status`        tinyint unsigned NOT NULL DEFAULT 0 COMMENT '状态：0-未发布，1-已发布',
    `sort`          tinyint unsigned NOT NULL DEFAULT 0 COMMENT '排序值（越大越靠前）',
    `deleted`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time`   timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_name_deleted` (`category_name`, `deleted`) COMMENT '类目名称唯一索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '类目管理表';

CREATE TABLE `tag`
(
    `id`          bigint unsigned  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tag_name`    varchar(120)     NOT NULL COMMENT '标签名称',
    `tag_type`    tinyint unsigned NOT NULL DEFAULT 1 COMMENT '标签类型：1-系统标签，2-自定义标签',
    `category_id` bigint unsigned  NOT NULL DEFAULT 0 COMMENT '类目ID',
    `status`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '状态：0-未发布，1-已发布',
    `deleted`     tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '标签管理表';

CREATE TABLE `article_tag`
(
    `id`          bigint unsigned  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`  bigint unsigned  NOT NULL DEFAULT 0 COMMENT '文章ID',
    `tag_id`      bigint unsigned  NOT NULL DEFAULT 0 COMMENT '标签ID',
    `deleted`     tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`) COMMENT '文章与标签唯一约束',
    KEY `idx_tag_id` (`tag_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '文章标签映射表';

CREATE TABLE `notify_msg`
(
    `id`              int(10) unsigned    NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `related_id`      int(10) unsigned    NOT NULL DEFAULT '0' COMMENT '关联的主键',
    `notify_user_id`  int(10) unsigned    NOT NULL DEFAULT '0' COMMENT '通知的用户id',
    `operate_user_id` int(10) unsigned    NOT NULL DEFAULT '0' COMMENT '触发这个通知的用户id',
    `msg`             varchar(1024)       NOT NULL DEFAULT '' COMMENT '消息内容',
    `type`            tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '类型: 0-默认，1-评论，2-回复 3-点赞 4-收藏 5-关注 6-系统',
    `state`           tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '阅读状态: 0-未读，1-已读',
    `create_time`     timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY `key_notify_user_id_type_state` (`notify_user_id`, `type`, `state`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1086
  DEFAULT CHARSET = utf8mb4 COMMENT ='消息通知列表';

CREATE TABLE `read_count`
(
    `id`           bigint unsigned  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `content_id`   bigint unsigned  NOT NULL DEFAULT 0 COMMENT '内容ID（文章或评论）',
    `content_type` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '内容类型：1-文章，2-评论',
    `cnt`          int unsigned     NOT NULL DEFAULT 0 COMMENT '访问计数',
    `create_time`  timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_content_id_type` (`content_id`, `content_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '内容访问计数表';

CREATE TABLE `request_count`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `host`        varchar(32)     NOT NULL DEFAULT '' COMMENT '机器IP地址',
    `cnt`         int unsigned    NOT NULL DEFAULT 0 COMMENT '访问计数',
    `date`        date            NOT NULL COMMENT '计数日期',
    `create_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_date_host` (`date`, `host`) COMMENT '同一主机同一天只记录一条'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '请求计数表';
