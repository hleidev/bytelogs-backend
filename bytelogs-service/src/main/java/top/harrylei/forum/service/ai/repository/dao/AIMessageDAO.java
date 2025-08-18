package top.harrylei.forum.service.ai.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.service.ai.repository.entity.AIMessageDO;
import top.harrylei.forum.service.ai.repository.mapper.AIMessageMapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI消息DAO
 *
 * @author harry
 */
@Repository
public class AIMessageDAO extends ServiceImpl<AIMessageMapper, AIMessageDO> {

    /**
     * 保存用户消息
     */
    public Long saveUserMessage(Long conversationId, Long userId, String content) {
        AIMessageDO message = new AIMessageDO();
        message.setConversationId(conversationId);
        message.setUserId(userId);
        message.setContent(content);

        save(message);
        return message.getId();
    }

    /**
     * 分页查询对话的消息列表
     */
    public IPage<AIMessageDO> pageQueryMessages(Long conversationId, IPage<AIMessageDO> page,
                                                LocalDateTime beforeTime) {
        return lambdaQuery()
                .eq(AIMessageDO::getConversationId, conversationId)
                .eq(AIMessageDO::getDeleted, YesOrNoEnum.NO.getCode())
                .lt(beforeTime != null, AIMessageDO::getCreateTime, beforeTime)
                .orderByDesc(AIMessageDO::getCreateTime)
                .page(page);
    }

    /**
     * 查询对话的最近几条消息（用于上下文）
     */
    public List<AIMessageDO> getRecentMessages(Long conversationId, Integer limit) {
        return lambdaQuery()
                .eq(AIMessageDO::getConversationId, conversationId)
                .eq(AIMessageDO::getDeleted, YesOrNoEnum.NO.getCode())
                .orderByDesc(AIMessageDO::getCreateTime)
                .last("LIMIT " + limit)
                .list();
    }

    /**
     * 软删除对话下的所有消息
     */
    public void deleteMessagesByConversationId(Long conversationId) {
        lambdaUpdate()
                .eq(AIMessageDO::getConversationId, conversationId)
                .set(AIMessageDO::getDeleted, YesOrNoEnum.YES.getCode())
                .update();
    }
}