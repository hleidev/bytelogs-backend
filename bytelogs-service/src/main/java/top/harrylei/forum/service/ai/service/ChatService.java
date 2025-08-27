package top.harrylei.forum.service.ai.service;

import top.harrylei.forum.api.enums.ai.ChatConversationStatusEnum;
import top.harrylei.forum.api.model.ai.dto.ChatConversationDTO;
import top.harrylei.forum.api.model.ai.dto.ChatMessageDTO;
import top.harrylei.forum.api.model.ai.req.ChatReq;
import top.harrylei.forum.api.model.ai.req.ConversationsQueryParam;
import top.harrylei.forum.api.model.ai.req.MessagesQueryParam;
import top.harrylei.forum.api.model.page.PageVO;

/**
 * AI服务接口
 *
 * @author harry
 */
public interface ChatService {

    /**
     * 发送聊天消息
     *
     * @param chatReq 聊天请求参数
     * @return AI回复的消息
     */
    ChatMessageDTO chat(ChatReq chatReq);

    /**
     * 获取用户的对话列表
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @param status 对话状态
     * @return 对话列表
     */
    PageVO<ChatConversationDTO> pageQueryConversations(Long userId, ConversationsQueryParam page, ChatConversationStatusEnum status);

    /**
     * 获取对话详情（包含消息历史）
     *
     * @param conversationId 对话ID
     * @param userId         用户ID
     * @return 对话详情
     */
    ChatConversationDTO getConversationDetail(Long conversationId, Long userId);

    /**
     * 获取对话的消息列表
     *
     * @param conversationId 对话ID
     * @param userId         用户ID
     * @param queryParam     分页参数
     * @return 消息列表
     */
    PageVO<ChatMessageDTO> pageQueryMessages(Long conversationId, Long userId, MessagesQueryParam queryParam);

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