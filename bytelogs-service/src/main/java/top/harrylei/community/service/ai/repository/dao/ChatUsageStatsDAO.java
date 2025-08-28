package top.harrylei.community.service.ai.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.community.service.ai.repository.entity.ChatUsageStatsDO;
import top.harrylei.community.service.ai.repository.mapper.ChatUsageStatsMapper;
import top.harrylei.community.api.enums.ai.ChatClientTypeEnum;

import java.time.LocalDate;

/**
 * AI使用统计DAO
 *
 * @author harry
 */
@Repository
public class ChatUsageStatsDAO extends ServiceImpl<ChatUsageStatsMapper, ChatUsageStatsDO> {

    /**
     * 根据用户ID和日期查询使用统计总量
     */
    public Long getTotalTokensByUserIdAndDate(Long userId, LocalDate date) {
        return lambdaQuery()
                .eq(ChatUsageStatsDO::getUserId, userId)
                .eq(ChatUsageStatsDO::getDate, date)
                .list()
                .stream()
                .mapToLong(ChatUsageStatsDO::getTotalTokens)
                .sum();
    }
    
    /**
     * 根据用户ID和日期查询消息总数
     */
    public Integer getTotalMessagesByUserIdAndDate(Long userId, LocalDate date) {
        return lambdaQuery()
                .eq(ChatUsageStatsDO::getUserId, userId)
                .eq(ChatUsageStatsDO::getDate, date)
                .list()
                .stream()
                .mapToInt(ChatUsageStatsDO::getMessageCount)
                .sum();
    }
    
    /**
     * 根据用户ID、日期、提供商和模型查询使用统计
     */
    public ChatUsageStatsDO getByUserIdDateProviderAndModel(Long userId, LocalDate date, 
                                                           ChatClientTypeEnum provider, String modelName) {
        return lambdaQuery()
                .eq(ChatUsageStatsDO::getUserId, userId)
                .eq(ChatUsageStatsDO::getDate, date)
                .eq(ChatUsageStatsDO::getProvider, provider)
                .eq(ChatUsageStatsDO::getModelName, modelName)
                .one();
    }
}