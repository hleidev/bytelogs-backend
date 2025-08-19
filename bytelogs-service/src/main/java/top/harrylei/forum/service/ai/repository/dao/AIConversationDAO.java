package top.harrylei.forum.service.ai.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.enums.ai.AIConversationStatusEnum;
import top.harrylei.forum.service.ai.repository.entity.AIConversationDO;
import top.harrylei.forum.service.ai.repository.mapper.AIConversationMapper;

import java.time.LocalDateTime;

/**
 * AI对话DAO
 *
 * @author harry
 */
@Repository
public class AIConversationDAO extends ServiceImpl<AIConversationMapper, AIConversationDO> {

    private final AIConversationMapper aIConversationMapper;

    public AIConversationDAO(AIConversationMapper aIConversationMapper) {
        this.aIConversationMapper = aIConversationMapper;
    }

    /**
     * 创建新对话
     */
    public Long createConversation(Long userId, String title) {
        AIConversationDO conversation = new AIConversationDO();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversation.setStatus(AIConversationStatusEnum.ACTIVE);
        conversation.setLastMessageTime(LocalDateTime.now());

        save(conversation);
        return conversation.getId();
    }


    /**
     * 根据ID和用户ID查询对话（确保权限）
     */
    public AIConversationDO getByIdAndUserId(Long id, Long userId) {
        return lambdaQuery()
                .eq(AIConversationDO::getId, id)
                .eq(AIConversationDO::getUserId, userId)
                .eq(AIConversationDO::getDeleted, YesOrNoEnum.NO.getCode())
                .one();
    }

    /**
     * 分页查询用户的对话列表
     */
    public IPage<AIConversationDO> pageQueryConversations(Long userId, IPage<AIConversationDO> page, AIConversationStatusEnum status) {
        return lambdaQuery()
                .eq(AIConversationDO::getUserId, userId)
                .eq(AIConversationDO::getDeleted, YesOrNoEnum.NO.getCode())
                .eq(AIConversationDO::getStatus, status.getCode())
                .orderByDesc(AIConversationDO::getLastMessageTime)
                .page(page);
    }

    /**
     * 更新对话的最后消息信息
     */
    public void updateLastMessage(Long conversationId, String messagePreview) {
        AIConversationDO conversation = new AIConversationDO();
        conversation.setId(conversationId);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversation.setLastMessagePreview(messagePreview);

        updateById(conversation);
    }

    /**
     * 增加消息数量
     */
    public void incrementMessageCount(Long conversationId, Integer increment) {
        aIConversationMapper.incrementMessageCount(conversationId, increment);
    }

    /**
     * 软删除对话
     */
    public boolean deleteConversation(Long conversationId, Long userId) {
        return lambdaUpdate()
                .eq(AIConversationDO::getId, conversationId)
                .eq(AIConversationDO::getUserId, userId)
                .eq(AIConversationDO::getDeleted, YesOrNoEnum.NO.getCode())
                .set(AIConversationDO::getDeleted, YesOrNoEnum.YES.getCode())
                .update();
    }

    /**
     * 归档对话
     */
    public boolean archiveConversation(Long conversationId, Long userId) {
        return lambdaUpdate()
                .eq(AIConversationDO::getId, conversationId)
                .eq(AIConversationDO::getUserId, userId)
                .ne(AIConversationDO::getStatus, AIConversationStatusEnum.ARCHIVED)
                .set(AIConversationDO::getStatus, AIConversationStatusEnum.ARCHIVED)
                .update();
    }
}