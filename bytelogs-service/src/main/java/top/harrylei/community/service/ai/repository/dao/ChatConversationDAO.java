package top.harrylei.community.service.ai.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.community.api.enums.YesOrNoEnum;
import top.harrylei.community.api.enums.ai.ChatConversationStatusEnum;
import top.harrylei.community.service.ai.repository.entity.ChatConversationDO;
import top.harrylei.community.service.ai.repository.mapper.ChatConversationMapper;

import java.time.LocalDateTime;

/**
 * AI对话DAO
 *
 * @author harry
 */
@Repository
public class ChatConversationDAO extends ServiceImpl<ChatConversationMapper, ChatConversationDO> {

    /**
     * 创建新对话
     */
    public Long createConversation(Long userId, String title) {
        ChatConversationDO conversation = new ChatConversationDO();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversation.setStatus(ChatConversationStatusEnum.ACTIVE);
        conversation.setLastMessageTime(LocalDateTime.now());

        save(conversation);
        return conversation.getId();
    }


    /**
     * 根据ID和用户ID查询对话（确保权限）
     */
    public ChatConversationDO getByIdAndUserId(Long id, Long userId) {
        return lambdaQuery()
                .eq(ChatConversationDO::getId, id)
                .eq(ChatConversationDO::getUserId, userId)
                .eq(ChatConversationDO::getDeleted, YesOrNoEnum.NO)
                .one();
    }

    /**
     * 分页查询用户的对话列表
     */
    public IPage<ChatConversationDO> pageQueryConversations(Long userId, IPage<ChatConversationDO> page, ChatConversationStatusEnum status) {
        return lambdaQuery()
                .eq(ChatConversationDO::getUserId, userId)
                .eq(ChatConversationDO::getDeleted, YesOrNoEnum.NO)
                .eq(ChatConversationDO::getStatus, status.getCode())
                .orderByDesc(ChatConversationDO::getLastMessageTime)
                .page(page);
    }

    /**
     * 更新对话的最后消息信息
     */
    public void updateLastMessage(Long conversationId, String messagePreview) {
        ChatConversationDO conversation = new ChatConversationDO();
        conversation.setId(conversationId);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversation.setLastMessagePreview(messagePreview);

        updateById(conversation);
    }

    /**
     * 软删除对话
     */
    public boolean deleteConversation(Long conversationId, Long userId) {
        return lambdaUpdate()
                .eq(ChatConversationDO::getId, conversationId)
                .eq(ChatConversationDO::getUserId, userId)
                .eq(ChatConversationDO::getDeleted, YesOrNoEnum.NO)
                .set(ChatConversationDO::getDeleted, YesOrNoEnum.YES)
                .update();
    }

    /**
     * 归档对话
     */
    public boolean archiveConversation(Long conversationId, Long userId) {
        return lambdaUpdate()
                .eq(ChatConversationDO::getId, conversationId)
                .eq(ChatConversationDO::getUserId, userId)
                .ne(ChatConversationDO::getStatus, ChatConversationStatusEnum.ARCHIVED)
                .set(ChatConversationDO::getStatus, ChatConversationStatusEnum.ARCHIVED)
                .update();
    }
}