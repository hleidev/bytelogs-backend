package top.harrylei.forum.core.common.constans;

/**
 * Redis键前缀常量类，统一管理所有Redis Key结构
 * 
 */
public class RedisKeyConstants {

    // 全局前缀
    public static final String GLOBAL_PREFIX = "byte_logs:";

    // 用户模块
    public static final String USER_MODULE = GLOBAL_PREFIX + "user:";
    public static final String USER_TOKEN = USER_MODULE + "token:"; // 用户令牌
    public static final String USER_INFO = USER_MODULE + "info:"; // 用户信息

    // 内容模块
    public static final String CONTENT_MODULE = GLOBAL_PREFIX + "content:";
    public static final String ARTICLE = CONTENT_MODULE + "article:"; // 文章
    public static final String COMMENT = CONTENT_MODULE + "comment:"; // 评论

    // 系统模块
    public static final String SYSTEM_MODULE = GLOBAL_PREFIX + "system:";
    public static final String CONFIG = SYSTEM_MODULE + "config:"; // 配置
    public static final String CACHE = SYSTEM_MODULE + "cache:"; // 缓存

    // 统计模块
    public static final String STAT_MODULE = GLOBAL_PREFIX + "stat:";
    public static final String VIEW_COUNT = STAT_MODULE + "view:"; // 浏览统计
    public static final String LIKE_COUNT = STAT_MODULE + "like:"; // 点赞统计

    // 限流模块
    public static final String LIMIT_MODULE = GLOBAL_PREFIX + "limit:";
    public static final String API_LIMIT = LIMIT_MODULE + "api:"; // API限流

    // 构造完整的键名（带ID）
    public static String getUserTokenKey(Long userId) {
        return USER_TOKEN + userId;
    }

    public static String getUserInfoKey(Long userId) {
        return USER_INFO + userId;
    }

    public static String getArticleKey(Long articleId) {
        return ARTICLE + articleId;
    }
}
