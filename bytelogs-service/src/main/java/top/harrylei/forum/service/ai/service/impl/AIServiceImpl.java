package top.harrylei.forum.service.ai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.harrylei.forum.api.enums.ResultCode;
import top.harrylei.forum.api.enums.ai.AIClientTypeEnum;
import top.harrylei.forum.api.enums.ai.AIMessageRoleEnum;
import top.harrylei.forum.api.model.ai.dto.AIConversationDTO;
import top.harrylei.forum.api.model.ai.dto.AIMessageDTO;
import top.harrylei.forum.api.model.ai.req.ConversationsQueryParam;
import top.harrylei.forum.api.model.ai.req.MessagesQueryParam;
import top.harrylei.forum.api.model.base.BaseDO;
import top.harrylei.forum.api.model.page.PageVO;
import top.harrylei.forum.core.config.AIConfig;
import top.harrylei.forum.core.config.AILimitConfig;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.util.PageUtils;
import top.harrylei.forum.service.ai.client.AIClientFactory;
import top.harrylei.forum.service.ai.client.ChatRequest;
import top.harrylei.forum.service.ai.client.ChatResponse;
import top.harrylei.forum.service.ai.converted.AIConversationStructMapper;
import top.harrylei.forum.service.ai.converted.AIMessageStructMapper;
import top.harrylei.forum.service.ai.repository.dao.AIConversationDAO;
import top.harrylei.forum.service.ai.repository.dao.AIMessageDAO;
import top.harrylei.forum.service.ai.repository.entity.AIConversationDO;
import top.harrylei.forum.service.ai.repository.entity.AIMessageDO;
import top.harrylei.forum.service.ai.service.AIService;
import top.harrylei.forum.service.ai.service.AIUsageService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * AI服务实现
 *
 * @author harry
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AIServiceImpl implements AIService {

    private final AIClientFactory aiClientFactory;
    private final AIConversationDAO AIConversationDAO;
    private final AIMessageDAO aiMessageDAO;
    private final AIUsageService aiUsageService;
    private final AIConfig aiConfig;
    private final AILimitConfig aiLimitConfig;
    private final AIMessageStructMapper aiMessageStructMapper;
    private final AIConversationStructMapper aiConversationStructMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AIMessageDTO chat(String message, Long conversationId, AIClientTypeEnum model) {
        Long userId = getCurrentUserId();
        log.info("用户发起AI对话，userId: {}, conversationId: {}, model: {}, message长度: {}",
                 userId, conversationId, model, message.length());

        // 1. 验证请求
        validateChatRequest(message, userId);

        // 2. 准备对话
        boolean isNewConversation = (conversationId == null);
        conversationId = prepareConversation(conversationId, userId, message);

        // 3. 执行AI调用
        ChatResponse response = executeAIChat(conversationId, message, model);

        // 4. 保存结果并返回
        return saveResults(conversationId, userId, response, model, isNewConversation);

    }

    @Override
    public PageVO<AIConversationDTO> pageQueryConversations(Long userId, ConversationsQueryParam queryParam) {
        // 参数校验
        if (userId == null) {
            ResultCode.AUTHENTICATION_FAILED.throwException("用户ID不能为空");
        }

        // 创建MyBatis-Plus分页对象
        IPage<AIConversationDO> page = PageUtils.of(queryParam);

        // 分页查询对话列表
        IPage<AIConversationDO> conversationPage = AIConversationDAO.pageQueryConversations(userId, page);

        // 转换为DTO并构建分页结果
        return PageUtils.from(conversationPage, aiConversationStructMapper::toDTO);
    }

    @Override
    public AIConversationDTO getConversationDetail(Long conversationId, Long userId) {
        // 验证权限并获取对话
        AIConversationDO aiConversation = AIConversationDAO.getByIdAndUserId(conversationId, userId);
        if (aiConversation == null) {
            ResultCode.AI_CONVERSATION_NOT_EXISTS.throwException();
        }

        return aiConversationStructMapper.toDTO(aiConversation);
    }

    @Override
    public PageVO<AIMessageDTO> pageQueryMessages(Long conversationId, Long userId,
                                                  MessagesQueryParam queryParam) {
        // 验证权限
        AIConversationDO conversation = AIConversationDAO.getByIdAndUserId(conversationId, userId);
        if (conversation == null) {
            ResultCode.AI_CONVERSATION_NOT_EXISTS.throwException();
        }

        // 创建MyBatis-Plus分页对象
        IPage<AIMessageDO> pageQuery = PageUtils.of(queryParam);

        // 分页查询消息列表
        IPage<AIMessageDO> messagePage = aiMessageDAO.pageQueryMessages(conversationId, pageQuery,
                                                                        queryParam.getBeforeTime());

        // 转换为DTO并构建分页结果
        return PageUtils.from(messagePage, aiMessageStructMapper::toDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long conversationId, Long userId) {
        log.info("删除对话，conversationId: {}, userId: {}", conversationId, userId);

        // 验证权限
        validateConversationAccess(conversationId, userId);

        // 软删除对话和相关消息
        boolean deleted = AIConversationDAO.deleteConversation(conversationId, userId);
        if (deleted) {
            aiMessageDAO.deleteMessagesByConversationId(conversationId);
        } else {
            log.info("对话可能已删除，conversationId: {}", conversationId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveConversation(Long conversationId, Long userId) {
        log.info("归档对话，conversationId: {}, userId: {}", conversationId, userId);

        // 验证权限
        validateConversationAccess(conversationId, userId);

        // 归档对话
        boolean archived = AIConversationDAO.archiveConversation(conversationId, userId);
        if (!archived) {
            log.info("对话可能已归档，conversationId: {}", conversationId);
        }
    }

    /**
     * 验证对话访问权限
     */
    private void validateConversationAccess(Long conversationId, Long userId) {
        AIConversationDO conversation = AIConversationDAO.getByIdAndUserId(conversationId, userId);
        if (conversation == null) {
            ResultCode.AI_CONVERSATION_NOT_EXISTS.throwException();
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        return ReqInfoContext.getContext().getUserId();
    }

    /**
     * 生成对话标题
     */
    private String generateConversationTitle(String message) {
        if (message.length() <= 20) {
            return message;
        }
        return message.substring(0, 20) + "...";
    }

    /**
     * 构建带上下文的聊天请求
     */
    private ChatRequest buildChatRequestWithContext(Long conversationId, String message) {
        ChatRequest request = new ChatRequest();

        List<ChatRequest.Message> messages = new ArrayList<>();

        // 获取最近的消息作为上下文（最多10条）
        List<AIMessageDO> recentMessages = aiMessageDAO.getRecentMessages(conversationId, 10);

        if (!CollectionUtils.isEmpty(recentMessages)) {
            recentMessages.stream()
                    .sorted(Comparator.comparing(BaseDO::getCreateTime))
                    .map(msg -> new ChatRequest.Message(msg.getRole(), msg.getContent()))
                    .forEach(messages::add);
        }

        // 添加当前用户消息
        messages.add(ChatRequest.Message.user(message));

        request.setMessages(messages);
        return request;
    }

    /**
     * 验证聊天请求
     */
    private void validateChatRequest(String message, Long userId) {
        // 验证消息长度限制
        if (message.length() > aiLimitConfig.getMaxMessageLength()) {
            ResultCode.AI_MESSAGE_TOO_LONG.throwException();
        }

        // 验证每小时使用量限制
        if (!aiUsageService.checkHourlyMessageLimit(userId)) {
            ResultCode.AI_HOURLY_LIMIT_EXCEEDED.throwException();
        }

        // 验证每日使用量限制
        if (!aiUsageService.checkDailyLimit(userId)) {
            ResultCode.AI_DAILY_LIMIT_EXCEEDED.throwException();
        }
    }

    /**
     * 准备对话（创建或验证）
     */
    private Long prepareConversation(Long conversationId, Long userId, String message) {
        // 如果是新对话，创建对话记录
        if (conversationId == null) {
            String title = generateConversationTitle(message);
            conversationId = AIConversationDAO.createConversation(userId, title);
            log.info("创建新对话，conversationId: {}, title: {}", conversationId, title);
            return conversationId;
        }

        // 验证现有对话的权限
        AIConversationDO conversation = AIConversationDAO.getByIdAndUserId(conversationId, userId);
        if (conversation == null) {
            ResultCode.AI_CONVERSATION_NOT_EXISTS.throwException();
        }

        return conversationId;
    }

    /**
     * 执行AI调用
     */
    private ChatResponse executeAIChat(Long conversationId, String message, AIClientTypeEnum model) {
        // 保存用户消息
        aiMessageDAO.saveUserMessage(conversationId, getCurrentUserId(), message);

        // 构建上下文消息
        ChatRequest request = buildChatRequestWithContext(conversationId, message);

        // 调用AI API
        ChatResponse response = aiClientFactory.getClient(model).chat(request);

        if (!response.isSuccess() || !StringUtils.hasText(response.getContent())) {
            ResultCode.AI_RESPONSE_EMPTY.throwException();
        }

        return response;
    }

    /**
     * 保存结果并返回
     */
    private AIMessageDTO saveResults(Long conversationId, Long userId,
                                     ChatResponse response, AIClientTypeEnum model, boolean isNewConversation) {
        String aiContent = response.getContent();

        // 构建AI消息DO对象
        AIMessageDO aiMessageDO = new AIMessageDO();
        aiMessageDO.setConversationId(conversationId);
        aiMessageDO.setUserId(userId);
        aiMessageDO.setRole(AIMessageRoleEnum.ASSISTANT);
        aiMessageDO.setContent(aiContent);
        aiMessageDO.setModel(model);
        aiMessageDO.setInputTokens(response.getInputTokens());
        aiMessageDO.setOutputTokens(response.getOutputTokens());
        aiMessageDO.setTotalTokens(response.getTotalTokens());
        aiMessageDO.setCreateTime(LocalDateTime.now());
        aiMessageDO.setUpdateTime(LocalDateTime.now());

        // 保存AI回复消息
        aiMessageDAO.save(aiMessageDO);

        // 更新对话的最后消息信息
        String preview = aiContent.length() > 60 ? aiContent.substring(0, 60) + "..." : aiContent;
        AIConversationDAO.updateLastMessage(conversationId, preview);

        // 增加消息数量（用户消息 + AI消息 = 2条）
        AIConversationDAO.incrementMessageCount(conversationId, 2);

        // 记录使用量统计
        Integer conversationIncrement = isNewConversation ? 1 : 0;
        aiUsageService.recordUsage(userId, 2, response.getTotalTokens(), conversationIncrement);

        // 转换为DTO并返回
        AIMessageDTO aiMessage = aiMessageStructMapper.toDTO(aiMessageDO);

        log.info("AI对话完成，conversationId: {}", conversationId);
        return aiMessage;
    }
}