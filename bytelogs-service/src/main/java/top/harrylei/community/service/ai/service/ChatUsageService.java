package top.harrylei.community.service.ai.service;

import top.harrylei.community.service.ai.repository.entity.ChatUsageStatsDO;
import top.harrylei.community.api.enums.ai.ChatClientTypeEnum;

import java.time.LocalDate;

/**
 * AI使用量统计服务接口
 *
 * @author harry
 */
public interface ChatUsageService {

    /**
     * 检查用户今日使用量是否超限
     *
     * @param userId 用户ID
     * @return true-未超限，false-已超限
     */
    boolean checkDailyLimit(Long userId);

    /**
     * 检查用户每小时消息数量是否超限
     *
     * @param userId 用户ID
     * @return true-未超限，false-已超限
     */
    boolean checkHourlyMessageLimit(Long userId);

    /**
     * 记录用户使用情况
     *
     * @param userId            用户ID
     * @param provider          AI提供商
     * @param modelName         模型名称
     * @param messageCount      消息数量增量
     * @param promptTokens      提示词Token消耗增量
     * @param completionTokens  完成Token消耗增量
     * @param totalTokens       总Token消耗增量
     * @param conversationCount 对话数量增量
     */
    void recordUsage(Long userId, ChatClientTypeEnum provider, String modelName, 
                    Integer messageCount, Long promptTokens, Long completionTokens,
                    Long totalTokens, Integer conversationCount);

    /**
     * 获取用户今日使用统计
     *
     * @param userId 用户ID
     * @param date   日期
     * @return 使用统计
     */
    ChatUsageStatsDO getDailyUsage(Long userId, LocalDate date);

    /**
     * 获取用户今日剩余可用消息数量
     *
     * @param userId 用户ID
     * @return 剩余消息数量
     */
    Integer getRemainingMessages(Long userId);
}