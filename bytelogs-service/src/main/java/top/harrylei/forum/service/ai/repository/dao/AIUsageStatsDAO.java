package top.harrylei.forum.service.ai.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.service.ai.repository.entity.AIUsageStatsDO;
import top.harrylei.forum.service.ai.repository.mapper.AIUsageStatsMapper;

import java.time.LocalDate;

/**
 * AI使用统计DAO
 *
 * @author harry
 */
@Repository
public class AIUsageStatsDAO extends ServiceImpl<AIUsageStatsMapper, AIUsageStatsDO> {

    /**
     * 根据用户ID和日期查询使用统计
     */
    public AIUsageStatsDO getByUserIdAndDate(Long userId, LocalDate date) {
        return lambdaQuery()
                .eq(AIUsageStatsDO::getUserId, userId)
                .eq(AIUsageStatsDO::getDate, date)
                .one();
    }
}