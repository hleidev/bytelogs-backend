package top.harrylei.community.service.ai.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import top.harrylei.community.service.ai.repository.entity.ChatConversationDO;

/**
 * AI对话Mapper接口
 *
 * @author harry
 */
@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversationDO> {

    @Update("update chat_conversation set message_count = message_count + #{increment} where id = #{conversationId}")
    void incrementMessageCount(Long conversationId, Integer increment);
}