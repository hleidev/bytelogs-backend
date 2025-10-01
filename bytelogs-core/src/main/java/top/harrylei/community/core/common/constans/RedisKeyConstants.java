package top.harrylei.community.core.common.constans;

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
    public static final String LOCK = GLOBAL_PREFIX + "lock:";
    public static final String KAFKA = GLOBAL_PREFIX + "kafka:";
    public static final String ACTIVITY = GLOBAL_PREFIX + "activity:";
    public static final String AI = GLOBAL_PREFIX + "ai:";
    public static final String ARTICLE = GLOBAL_PREFIX + "article:";

    // 功能分类
    public static final String USER_TOKEN = USER + "token:";
    public static final String USER_INFO = USER + "info:";
    public static final String DISTRIBUTED_LOCK = LOCK + "distributed:";
    public static final String DUPLICATE_LOCK = LOCK + "duplicate:";
    public static final String KAFKA_IDEMPOTENCY = KAFKA + "idempotency:";
    public static final String ACTIVITY_RANK = ACTIVITY + "rank:";
    public static final String ACTIVITY_DAILY = ACTIVITY + "idempotency:";
    public static final String HEALTH_CHECK = GLOBAL_PREFIX + "health:check";
    public static final String AI_HOURLY_LIMIT = AI + "hourly_limit:";
    public static final String AI_DAILY_USAGE = AI + "daily_usage:";
    public static final String AI_CHAT_CONTEXT = AI + "chat_context:";
    public static final String ARTICLE_READ_COUNT_LOCK = ARTICLE + "read_count_lock:";


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
     * 构建Kafka幂等性检查key
     *
     * @param eventId 事件ID
     * @return Kafka幂等性检查key
     */
    public static String getKafkaIdempotencyKey(String eventId) {
        return KAFKA_IDEMPOTENCY + eventId;
    }

    /**
     * 构建用户活跃度总排行榜key
     *
     * @return 总排行榜key
     */
    public static String getActivityTotalRankKey() {
        return ACTIVITY_RANK + "total";
    }

    /**
     * 构建用户活跃度日排行榜key
     *
     * @param date 日期(yyyy-MM-dd格式)
     * @return 日排行榜key
     */
    public static String getActivityDailyRankKey(String date) {
        return ACTIVITY_RANK + "daily:" + date;
    }

    /**
     * 构建AI每小时限制key
     *
     * @param userId 用户ID
     * @return AI每小时限制key
     */
    public static String getChatHourlyLimitKey(Long userId) {
        return AI_HOURLY_LIMIT + userId;
    }

    /**
     * 构建AI每日使用量缓存key
     *
     * @param userId 用户ID
     * @param date   日期(yyyy-MM-dd格式)
     * @return AI每日使用量key
     */
    public static String getChatDailyUsageKey(Long userId, String date) {
        return AI_DAILY_USAGE + userId + ":" + date;
    }

    /**
     * 构建AI对话上下文缓存key
     *
     * @param conversationId 对话ID
     * @return AI对话上下文缓存key
     */
    public static String getChatContextKey(Long conversationId) {
        return AI_CHAT_CONTEXT + conversationId;
    }

    /**
     * 构建用户活跃度月排行榜key
     *
     * @param yearMonth 年月(yyyy-MM格式)
     * @return 月排行榜key
     */
    public static String getActivityMonthlyRankKey(String yearMonth) {
        return ACTIVITY_RANK + "monthly:" + yearMonth;
    }


    /**
     * 构建用户每日活跃度Hash key
     *
     * @param userId 用户ID
     * @param date   日期(yyyy-MM-dd格式)
     * @return 用户每日活跃度Hash key
     */
    public static String getActivityDailyKey(Long userId, String date) {
        return ACTIVITY_DAILY + date + ":" + userId;
    }

    /**
     * 构建文章阅读量统计防重复锁key
     *
     * @param articleId  文章ID
     * @param identifier 标识符(用户ID或IP)
     * @param type       类型(user或ip)
     * @return 文章阅读量统计防重复锁key
     */
    public static String getArticleReadCountLockKey(Long articleId, String identifier, String type) {
        return ARTICLE_READ_COUNT_LOCK + articleId + ":" + type + ":" + identifier;
    }
}
