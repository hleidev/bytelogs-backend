package top.harrylei.community.service.ai.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.service.ai.repository.entity.ChatMessageDO;
import top.harrylei.community.service.ai.repository.mapper.ChatMessageMapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI消息DAO
 *
 * @author harry
 */
@Repository
public class ChatMessageDAO extends ServiceImpl<ChatMessageMapper, ChatMessageDO> {
    /**
     * 分页查询对话的消息列表
     */
    public IPage<ChatMessageDO> pageQueryMessages(Long conversationId, IPage<ChatMessageDO> page,
                                                  LocalDateTime beforeTime) {
        return lambdaQuery()
                .eq(ChatMessageDO::getConversationId, conversationId)
                .eq(ChatMessageDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .lt(beforeTime != null, ChatMessageDO::getCreateTime, beforeTime)
                .orderByDesc(ChatMessageDO::getCreateTime)
                .page(page);
    }

    /**
     * 查询对话的最近几条消息
     */
    public List<ChatMessageDO> getRecentMessages(Long conversationId, Integer limit) {
        return lambdaQuery()
                .eq(ChatMessageDO::getConversationId, conversationId)
                .eq(ChatMessageDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .orderByDesc(ChatMessageDO::getCreateTime)
                .last("LIMIT " + limit)
                .list();
    }

    /**
     * 软删除对话下的所有消息
     */
    public void deleteByConversationId(Long conversationId) {
        lambdaUpdate()
                .eq(ChatMessageDO::getConversationId, conversationId)
                .eq(ChatMessageDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .set(ChatMessageDO::getDeleted, DeleteStatusEnum.DELETED)
                .update();
    }
}