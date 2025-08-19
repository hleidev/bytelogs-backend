package top.harrylei.forum.service.ai.service;

import top.harrylei.forum.api.enums.ai.AIClientTypeEnum;
import top.harrylei.forum.api.enums.ai.AIConversationStatusEnum;
import top.harrylei.forum.api.model.ai.dto.AIConversationDTO;
import top.harrylei.forum.api.model.ai.dto.AIMessageDTO;
import top.harrylei.forum.api.model.ai.req.ConversationsQueryParam;
import top.harrylei.forum.api.model.ai.req.MessagesQueryParam;
import top.harrylei.forum.api.model.page.PageVO;

/**
 * AI服务接口
 *
 * @author harry
 */
public interface AIService {

    /**
     * 发送聊天消息
     *
     * @param message        用户消息
     * @param conversationId 对话ID（可选，新对话时为null）
     * @param model          AI客户端类型
     * @return AI回复的消息
     */
    AIMessageDTO chat(String message, Long conversationId, AIClientTypeEnum model);

    /**
     * 获取用户的对话列表
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @param status 对话状态
     * @return 对话列表
     */
    PageVO<AIConversationDTO> pageQueryConversations(Long userId, ConversationsQueryParam page, AIConversationStatusEnum status);

    /**
     * 获取对话详情（包含消息历史）
     *
     * @param conversationId 对话ID
     * @param userId         用户ID
     * @return 对话详情
     */
    AIConversationDTO getConversationDetail(Long conversationId, Long userId);

    /**
     * 获取对话的消息列表
     *
     * @param conversationId 对话ID
     * @param userId         用户ID
     * @param queryParam     分页参数
     * @return 消息列表
     */
    PageVO<AIMessageDTO> pageQueryMessages(Long conversationId, Long userId, MessagesQueryParam queryParam);

    /**
     * 删除对话
     *
     * @param conversationId 对话ID
     * @param userId         用户ID
     */
    void deleteConversation(Long conversationId, Long userId);

    /**
     * 归档对话
     *
     * @param conversationId 对话ID
     * @param userId         用户ID
     */
    void archiveConversation(Long conversationId, Long userId);
}