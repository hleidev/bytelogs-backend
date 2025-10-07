package top.harrylei.community.service.statistics.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import top.harrylei.community.api.event.ArticleStatisticsEvent;
import top.harrylei.community.core.common.constans.KafkaTopics;
import top.harrylei.community.core.common.constans.RedisKeyConstants;
import top.harrylei.community.core.util.RedisUtil;
import top.harrylei.community.service.statistics.repository.dao.ArticleStatisticsDAO;

import java.time.Duration;

/**
 * 文章统计事件监听器
 *
 * @author harry
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleStatisticsEventListener {

    private final ArticleStatisticsDAO articleStatisticsDAO;
    private final RedisUtil redisUtil;

    /**
     * 处理文章统计事件
     *
     * @param event 文章统计事件
     */
    @KafkaListener(topics = KafkaTopics.ARTICLE_STATISTICS_EVENTS, containerFactory = "articleStatisticsKafkaListenerContainerFactory")
    public void handleArticleStatisticsEvent(ArticleStatisticsEvent event) {
        try {
            log.debug("收到文章统计事件: eventId={}, articleId={}, actionType={}",
                    event.getEventId(), event.getArticleId(), event.getActionType().getLabel());

            // 处理不同类型的统计操作
            switch (event.getActionType()) {
                case INCREMENT_READ -> handleReadIncrement(event);
                case INCREMENT_PRAISE -> articleStatisticsDAO.incrementPraiseCount(event.getArticleId());
                case DECREMENT_PRAISE -> articleStatisticsDAO.decrementPraiseCount(event.getArticleId());
                case INCREMENT_COLLECT -> articleStatisticsDAO.incrementCollectCount(event.getArticleId());
                case DECREMENT_COLLECT -> articleStatisticsDAO.decrementCollectCount(event.getArticleId());
                case INCREMENT_COMMENT -> articleStatisticsDAO.incrementCommentCount(event.getArticleId());
                case DECREMENT_COMMENT -> articleStatisticsDAO.decrementCommentCount(event.getArticleId());
                default -> log.warn("未知的统计操作类型: {}", event.getActionType());
            }

            log.debug("文章统计事件处理完成: eventId={}, articleId={}, actionType={}",
                    event.getEventId(), event.getArticleId(), event.getActionType().getLabel());

        } catch (Exception e) {
            log.error("处理文章统计事件失败: eventId={}, articleId={}, actionType={}",
                    event.getEventId(), event.getArticleId(), event.getActionType(), e);
            // 统计事件处理失败不抛出异常，避免影响其他消息处理
        }
    }

    /**
     * 处理阅读量增加事件（带防重复逻辑）
     *
     * @param event 文章统计事件
     */
    private void handleReadIncrement(ArticleStatisticsEvent event) {
        String lockKey = buildReadLockKey(event);
        Duration duration = getLockExpiration(event);

        try {
            if (redisUtil.setIfAbsent(lockKey, "1", duration)) {
                articleStatisticsDAO.incrementReadCount(event.getArticleId());
                log.debug("文章阅读量统计成功: articleId={}", event.getArticleId());
            } else {
                log.debug("重复访问，跳过统计: articleId={}", event.getArticleId());
            }
        } catch (Exception e) {
            log.error("文章阅读量统计失败: articleId={}", event.getArticleId(), e);
        }
    }

    /**
     * 构建阅读防重复锁Key
     *
     * @param event 文章统计事件
     * @return 锁Key
     */
    private String buildReadLockKey(ArticleStatisticsEvent event) {
        String extra = event.getExtra();
        if (extra != null && extra.startsWith("user:")) {
            // 登录用户：精确到用户
            String userId = extra.substring(5);
            return RedisKeyConstants.getArticleReadCountLockKey(event.getArticleId(), userId, "user");
        } else if (extra != null && extra.startsWith("ip:")) {
            // 未登录用户：按IP粗粒度控制
            String ip = extra.substring(3);
            return RedisKeyConstants.getArticleReadCountLockKey(event.getArticleId(), ip, "ip");
        } else {
            // 兜底：使用事件ID作为唯一标识
            return RedisKeyConstants.getArticleReadCountLockKey(event.getArticleId(), event.getEventId(), "event");
        }
    }

    /**
     * 获取锁过期时间
     *
     * @param event 文章统计事件
     * @return 过期时间
     */
    private Duration getLockExpiration(ArticleStatisticsEvent event) {
        String extra = event.getExtra();
        if (extra != null && extra.startsWith("user:")) {
            return Duration.ofHours(24);
        } else {
            return Duration.ofHours(1);
        }
    }
}