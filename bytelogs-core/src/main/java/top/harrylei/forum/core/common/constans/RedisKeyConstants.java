package top.harrylei.forum.core.common.constans;

/**
 * Redis键前缀常量类，统一管理所有Redis Key结构
 *
 * @author harry
 */
public class RedisKeyConstants {

    // 基础前缀
    public static final String GLOBAL_PREFIX = "byte_logs:";

    // 模块前缀
    public static final String USER = GLOBAL_PREFIX + "user:";
    public static final String ARTICLE = GLOBAL_PREFIX + "article:";
    public static final String LOCK = GLOBAL_PREFIX + "lock:";

    // 功能分类
    public static final String USER_TOKEN = USER + "token:";
    public static final String USER_INFO = USER + "info:";
    public static final String ARTICLE_EDIT_LOCK = ARTICLE + "edit_lock:";
    public static final String DISTRIBUTED_LOCK = LOCK + "distributed:";
    public static final String DUPLICATE_LOCK = LOCK + "duplicate:";


    /**
     * 构建用户令牌key
     *
     * @param userId 用户ID
     * @return 用户令牌key
     */
    public static String getUserTokenKey(Long userId) {
        return USER_TOKEN + userId;
    }

    /**
     * 构建用户信息key
     *
     * @param userId 用户ID
     * @return 用户信息key
     */
    public static String getUserInfoKey(Long userId) {
        return USER_INFO + userId;
    }

    /**
     * 构建分布式锁key
     *
     * @param lockKey 锁标识
     * @return 分布式锁key
     */
    public static String getDistributedLockKey(String lockKey) {
        return DISTRIBUTED_LOCK + lockKey;
    }

    /**
     * 构建防重复提交锁key
     *
     * @param lockKey 锁标识
     * @return 防重复提交锁key
     */
    public static String getDuplicateLockKey(String lockKey) {
        return DUPLICATE_LOCK + lockKey;
    }

    /**
     * 构建文章编辑锁key
     *
     * @param articleId 文章ID
     * @return 文章编辑锁key
     */
    public static String getArticleEditLockKey(Long articleId) {
        return ARTICLE_EDIT_LOCK + articleId;
    }
}
