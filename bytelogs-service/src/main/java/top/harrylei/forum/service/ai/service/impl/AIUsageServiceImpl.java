package top.harrylei.forum.service.ai.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;
import top.harrylei.forum.core.config.AILimitConfig;
import top.harrylei.forum.core.util.RedisUtil;
import top.harrylei.forum.service.ai.repository.dao.AIUsageStatsDAO;
import top.harrylei.forum.service.ai.repository.entity.AIUsageStatsDO;
import top.harrylei.forum.service.ai.service.AIUsageService;

import java.time.Duration;
import java.time.LocalDate;

/**
 * AI使用量统计服务实现
 *
 * @author harry
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AIUsageServiceImpl implements AIUsageService {


    private final AILimitConfig aiLimitConfig;
    private final AIUsageStatsDAO aiUsageStatsDAO;
    private final RedisUtil redisUtil;

    @Override
    public boolean checkDailyLimit(Long userId) {
        if (!aiLimitConfig.getEnabled()) {
            return true;
        }

        AIUsageStatsDO todayUsage = getDailyUsage(userId, LocalDate.now());
        if (todayUsage == null) {
            return true;
        }

        // 检查每日消息限制
        if (todayUsage.getMessageCount() >= aiLimitConfig.getDailyMessageLimit()) {
            log.warn("用户{}今日消息数量已超限: {}/{}",
                     userId, todayUsage.getMessageCount(), aiLimitConfig.getDailyMessageLimit());
            return false;
        }

        // 检查每日Token限制
        if (todayUsage.getTokensUsed() >= aiLimitConfig.getDailyTokenLimit()) {
            log.warn("用户{}今日Token消耗已超限: {}/{}",
                     userId, todayUsage.getTokensUsed(), aiLimitConfig.getDailyTokenLimit());
            return false;
        }

        return true;
    }

    @Override
    public boolean checkHourlyMessageLimit(Long userId) {
        if (!aiLimitConfig.getEnabled()) {
            return true;
        }

        String key = RedisKeyConstants.getAIHourlyLimitKey(userId);
        Integer currentCount = redisUtil.get(key, Integer.class);
        currentCount = currentCount != null ? currentCount : 0;

        return currentCount < aiLimitConfig.getHourlyMessageLimit();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordUsage(Long userId, Integer messageCount, Integer tokensUsed, Integer conversationCount) {
        LocalDate today = LocalDate.now();

        // 查询或创建今日使用统计
        AIUsageStatsDO usageStats = getDailyUsageFromDB(userId, today);
        if (usageStats == null) {
            usageStats = new AIUsageStatsDO();
            usageStats.setUserId(userId);
            usageStats.setDate(today);
            usageStats.setMessageCount(messageCount != null ? messageCount : 0);
            usageStats.setTokensUsed(tokensUsed != null ? tokensUsed : 0);
            usageStats.setConversationCount(conversationCount != null ? conversationCount : 0);

            aiUsageStatsDAO.save(usageStats);
        } else {
            // 更新统计数据
            usageStats.setMessageCount(usageStats.getMessageCount() + (messageCount != null ? messageCount : 0));
            usageStats.setTokensUsed(usageStats.getTokensUsed() + (tokensUsed != null ? tokensUsed : 0));
            usageStats.setConversationCount(usageStats.getConversationCount() + (conversationCount != null ? conversationCount : 0));

            aiUsageStatsDAO.updateById(usageStats);
        }

        // 更新每小时限制计数器
        if (messageCount != null && messageCount > 0) {
            updateHourlyCounter(userId, messageCount);
        }

        // 清除缓存
        clearDailyUsageCache(userId, today);

        log.info("记录用户{}使用量: messages={}, tokens={}, conversations={}",
                 userId, messageCount, tokensUsed, conversationCount);
    }

    @Override
    public AIUsageStatsDO getDailyUsage(Long userId, LocalDate date) {
        // 先从缓存查询
        String cacheKey = RedisKeyConstants.getAIDailyUsageKey(userId, date.toString());
        AIUsageStatsDO cachedUsage = redisUtil.get(cacheKey, AIUsageStatsDO.class);
        if (cachedUsage != null) {
            return cachedUsage;
        }

        // 从数据库查询
        AIUsageStatsDO usage = getDailyUsageFromDB(userId, date);
        if (usage != null) {
            // 缓存30分钟
            redisUtil.set(cacheKey, usage, Duration.ofMinutes(30));
        }

        return usage;
    }

    @Override
    public Integer getRemainingMessages(Long userId) {
        if (!aiLimitConfig.getEnabled()) {
            return Integer.MAX_VALUE;
        }

        AIUsageStatsDO todayUsage = getDailyUsage(userId, LocalDate.now());
        int used = todayUsage != null ? todayUsage.getMessageCount() : 0;

        return Math.max(0, aiLimitConfig.getDailyMessageLimit() - used);
    }

    /**
     * 从数据库查询每日使用统计
     */
    private AIUsageStatsDO getDailyUsageFromDB(Long userId, LocalDate date) {
        return aiUsageStatsDAO.getByUserIdAndDate(userId, date);
    }

    /**
     * 更新每小时计数器
     */
    private void updateHourlyCounter(Long userId, Integer messageCount) {
        String key = RedisKeyConstants.getAIHourlyLimitKey(userId);

        // 先尝试自增，如果key不存在会自动创建并设置为0+messageCount
        Long result = redisUtil.incrBy(key, messageCount);

        // 如果是第一次创建这个key（result等于messageCount），设置过期时间
        if (result.equals(messageCount.longValue())) {
            redisUtil.expire(key, Duration.ofHours(1));
        }
    }

    /**
     * 清除每日使用量缓存
     */
    private void clearDailyUsageCache(Long userId, LocalDate date) {
        String cacheKey = RedisKeyConstants.getAIDailyUsageKey(userId, date.toString());
        redisUtil.del(cacheKey);
    }
}