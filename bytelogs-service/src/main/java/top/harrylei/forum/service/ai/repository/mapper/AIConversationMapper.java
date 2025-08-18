package top.harrylei.forum.service.ai.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import top.harrylei.forum.service.ai.repository.entity.AIConversationDO;

/**
 * AI对话Mapper接口
 *
 * @author harry
 */
@Mapper
public interface AIConversationMapper extends BaseMapper<AIConversationDO> {

    @Update("update ai_conversation set message_count = message_count + #{increment} where id = #{conversationId}")
    void incrementMessageCount(Long conversationId, Integer increment);
}