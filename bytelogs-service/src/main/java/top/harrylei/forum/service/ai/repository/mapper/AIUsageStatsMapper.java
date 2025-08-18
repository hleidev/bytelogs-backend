package top.harrylei.forum.service.ai.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.harrylei.forum.service.ai.repository.entity.AIUsageStatsDO;

/**
 * AI使用统计Mapper接口
 *
 * @author harry
 */
@Mapper
public interface AIUsageStatsMapper extends BaseMapper<AIUsageStatsDO> {
}