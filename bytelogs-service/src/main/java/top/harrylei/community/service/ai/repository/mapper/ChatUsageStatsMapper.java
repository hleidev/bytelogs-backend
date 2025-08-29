package top.harrylei.community.service.ai.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.harrylei.community.api.model.ai.dto.ChatUsageStatsDTO;
import top.harrylei.community.service.ai.repository.entity.ChatUsageStatsDO;

/**
 * AI使用统计Mapper接口
 *
 * @author harry
 */
@Mapper
public interface ChatUsageStatsMapper extends BaseMapper<ChatUsageStatsDO> {

    ChatUsageStatsDTO toDTO(ChatUsageStatsDO usage);
}