package top.harrylei.forum.service.ai.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;
import top.harrylei.forum.core.config.AILimitConfig;
import top.harrylei.forum.core.util.RedisUtil;
import top.harrylei.forum.service.ai.repository.dao.ChatUsageStatsDAO;
import top.harrylei.forum.service.ai.repository.entity.ChatUsageStatsDO;
import top.harrylei.forum.service.ai.service.ChatUsageService;
import top.harrylei.forum.api.enums.ai.ChatClientTypeEnum;

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
public class ChatUsageServiceImpl implements ChatUsageService {


    private final AILimitConfig aiLimitConfig;
    private final ChatUsageStatsDAO chatUsageStatsDAO;
    private final RedisUtil redisUtil;

    @Override
    public boolean checkDailyLimit(Long userId) {
        if (!aiLimitConfig.getEnabled()) {
            return true;
        }

        LocalDate today = LocalDate.now();
        
        // 检查每日消息限制
        Integer totalMessages = chatUsageStatsDAO.getTotalMessagesByUserIdAndDate(userId, today);
        if (totalMessages >= aiLimitConfig.getDailyMessageLimit()) {
            log.warn("用户{}今日消息数量已超限: {}/{}",
                    userId, totalMessages, aiLimitConfig.getDailyMessageLimit());
            return false;
        }

        // 检查每日Token限制
        Long totalTokens = chatUsageStatsDAO.getTotalTokensByUserIdAndDate(userId, today);
        if (totalTokens >= aiLimitConfig.getDailyTokenLimit()) {
            log.warn("用户{}今日Token消耗已超限: {}/{}",
                    userId, totalTokens, aiLimitConfig.getDailyTokenLimit());
            return false;
        }

        return true;
    }

    @Override
    public boolean checkHourlyMessageLimit(Long userId) {
        if (!aiLimitConfig.getEnabled()) {
            return true;
        }

        String key = RedisKeyConstants.getChatHourlyLimitKey(userId);
        Integer currentCount = redisUtil.get(key, Integer.class);
        currentCount = currentCount != null ? currentCount : 0;

        return currentCount < aiLimitConfig.getHourlyMessageLimit();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordUsage(Long userId, ChatClientTypeEnum provider, String modelName,
                           Integer messageCount, Long promptTokens, Long completionTokens,
                           Long totalTokens, Integer conversationCount) {
        LocalDate today = LocalDate.now();

        ChatUsageStatsDO usageStats = getDailyUsageFromDB(userId, today, provider, modelName);
        if (usageStats == null) {
            usageStats = new ChatUsageStatsDO();
            usageStats.setUserId(userId);
            usageStats.setDate(today);
            usageStats.setProvider(provider);
            usageStats.setModelName(modelName);
            usageStats.setMessageCount(messageCount != null ? messageCount : 0);
            usageStats.setPromptTokens(promptTokens != null ? promptTokens : 0L);
            usageStats.setCompletionTokens(completionTokens != null ? completionTokens : 0L);
            usageStats.setTotalTokens(totalTokens != null ? totalTokens : 0L);
            usageStats.setConversationCount(conversationCount != null ? conversationCount : 0);

            chatUsageStatsDAO.save(usageStats);
        } else {
            usageStats.setMessageCount(usageStats.getMessageCount() + (messageCount != null ? messageCount : 0));
            usageStats.setPromptTokens(usageStats.getPromptTokens() + (promptTokens != null ? promptTokens : 0L));
            usageStats.setCompletionTokens(usageStats.getCompletionTokens() + (completionTokens != null ? completionTokens : 0L));
            usageStats.setTotalTokens(usageStats.getTotalTokens() + (totalTokens != null ? totalTokens : 0L));
            usageStats.setConversationCount(usageStats.getConversationCount() + (conversationCount != null ? conversationCount : 0));

            chatUsageStatsDAO.updateById(usageStats);
        }

        // 更新每小时限制计数器
        if (messageCount != null && messageCount > 0) {
            updateHourlyCounter(userId, messageCount);
        }

        // 清除缓存
        clearDailyUsageCache(userId, today);

        log.info("记录用户{}使用量: messages={}, promptTokens={}, completionTokens={}, totalTokens={}, conversations={}",
                userId, messageCount, promptTokens, completionTokens, totalTokens, conversationCount);
    }

    @Override
    public ChatUsageStatsDO getDailyUsage(Long userId, LocalDate date) {
        String cacheKey = RedisKeyConstants.getChatDailyUsageKey(userId, date.toString());
        ChatUsageStatsDO cachedUsage = redisUtil.get(cacheKey, ChatUsageStatsDO.class);
        if (cachedUsage != null) {
            return cachedUsage;
        }

        Long totalTokens = chatUsageStatsDAO.getTotalTokensByUserIdAndDate(userId, date);
        Integer totalMessages = chatUsageStatsDAO.getTotalMessagesByUserIdAndDate(userId, date);
        
        if (totalTokens == 0 && totalMessages == 0) {
            return null;
        }
        
        ChatUsageStatsDO usage = new ChatUsageStatsDO();
        usage.setUserId(userId);
        usage.setDate(date);
        usage.setTotalTokens(totalTokens);
        usage.setMessageCount(totalMessages);
        usage.setConversationCount(0);
        
        redisUtil.set(cacheKey, usage, Duration.ofMinutes(30));

        return usage;
    }

    @Override
    public Integer getRemainingMessages(Long userId) {
        if (!aiLimitConfig.getEnabled()) {
            return Integer.MAX_VALUE;
        }

        Integer used = chatUsageStatsDAO.getTotalMessagesByUserIdAndDate(userId, LocalDate.now());

        return Math.max(0, aiLimitConfig.getDailyMessageLimit() - used);
    }

    
    /**
     * 根据用户ID、日期、提供商和模型获取统计数据
     */
    private ChatUsageStatsDO getDailyUsageFromDB(Long userId, LocalDate date, 
                                                ChatClientTypeEnum provider, String modelName) {
        return chatUsageStatsDAO.getByUserIdDateProviderAndModel(userId, date, provider, modelName);
    }

    /**
     * 更新每小时计数器
     */
    private void updateHourlyCounter(Long userId, Integer messageCount) {
        String key = RedisKeyConstants.getChatHourlyLimitKey(userId);

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
        String cacheKey = RedisKeyConstants.getChatDailyUsageKey(userId, date.toString());
        redisUtil.del(cacheKey);
    }
}