package top.harrylei.forum.core.common.constans;

/**
 * Kafka Topic 常量定义
 *
 * @author harry
 */
public class KafkaTopics {

    /**
     * 私有构造器，防止实例化
     */
    private KafkaTopics() {
        throw new IllegalStateException("Constants class");
    }

    /**
     * 通知事件 Topic
     * 用于处理用户行为通知（点赞、评论、关注等）
     */
    public static final String NOTIFICATION_EVENTS = "bytelogs-notification-events";

    /**
     * 活跃度事件 Topic
     * 用于处理用户活跃度相关事件（文章、用户、评论等）
     */
    public static final String USER_ACTIVITY_EVENTS = "bytelogs-user-activity-events";

    /**
     * 系统事件 Topic
     * 用于处理系统级事件（注册、登录等）
     */
    public static final String SYSTEM_EVENTS = "bytelogs-system-events";
}