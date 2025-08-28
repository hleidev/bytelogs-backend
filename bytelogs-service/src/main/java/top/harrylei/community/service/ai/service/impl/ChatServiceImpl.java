package top.harrylei.community.service.ai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.harrylei.community.api.enums.ResultCode;
import top.harrylei.community.api.enums.ai.ChatClientTypeEnum;
import top.harrylei.community.api.enums.ai.ChatConversationStatusEnum;
import top.harrylei.community.api.enums.ai.ChatMessageRoleEnum;
import top.harrylei.community.api.model.ai.dto.ChatConversationDTO;
import top.harrylei.community.api.model.ai.dto.ChatMessageDTO;
import top.harrylei.community.api.model.ai.req.ChatReq;
import top.harrylei.community.api.model.ai.req.ConversationsQueryParam;
import top.harrylei.community.api.model.ai.req.MessagesQueryParam;
import top.harrylei.community.api.model.ai.vo.ChatResult;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.core.config.AILimitConfig;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.PageUtils;
import top.harrylei.community.service.ai.converted.ChatConversationStructMapper;
import top.harrylei.community.service.ai.converted.ChatMessageStructMapper;
import top.harrylei.community.service.ai.repository.dao.ChatConversationDAO;
import top.harrylei.community.service.ai.repository.dao.ChatMessageDAO;
import top.harrylei.community.service.ai.repository.entity.ChatConversationDO;
import top.harrylei.community.service.ai.repository.entity.ChatMessageDO;
import top.harrylei.community.service.ai.service.ChatService;
import top.harrylei.community.service.ai.service.ChatUsageService;

import java.util.ArrayList;
import java.util.List;

/**
 * AI服务实现 - 基于Spring AI
 *
 * @author harry
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {

    @Qualifier("deepseekChatClient")
    private final ChatClient deepseekChatClient;
    @Qualifier("qwenChatClient")
    private final ChatClient qwenChatClient;

    private final ChatConversationDAO chatConversationDAO;
    private final ChatMessageDAO chatMessageDAO;
    private final ChatUsageService chatUsageService;
    private final AILimitConfig aiLimitConfig;
    private final ChatMessageStructMapper chatMessageStructMapper;
    private final ChatConversationStructMapper chatConversationStructMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageDTO chat(ChatReq chatReq) {
        Long userId = getCurrentUserId();
        String userMessage = chatReq.getMessage();

        log.info("用户发起AI对话，userId: {}, conversationId: {}, message长度: {}",
                userId, chatReq.getConversationId(), userMessage.length());

        // 1. 验证请求
        validateChatRequest(userMessage, userId);

        // 2. 准备对话
        boolean isNewConversation = (chatReq.getConversationId() == null);
        Long conversationId = prepareConversation(chatReq.getConversationId(), userId, userMessage);

        // 3. 保存用户消息
        saveUserMessage(conversationId, userId, userMessage);

        // 4. 构建聊天上下文并调用AI
        ChatResult chatResult = executeChat(conversationId, userMessage, chatReq);

        // 5. 保存AI回复并返回
        return saveChatResponse(conversationId, userId, chatResult, isNewConversation);
    }

    @Override
    public PageVO<ChatConversationDTO> pageQueryConversations(Long userId, ConversationsQueryParam queryParam, ChatConversationStatusEnum status) {
        if (userId == null) {
            ResultCode.AUTHENTICATION_FAILED.throwException("用户ID不能为空");
        }
        if (status == null) {
            status = ChatConversationStatusEnum.ACTIVE;
        }

        IPage<ChatConversationDO> page = PageUtils.of(queryParam);
        IPage<ChatConversationDO> conversationPage = chatConversationDAO.pageQueryConversations(userId, page, status);
        return PageUtils.from(conversationPage, chatConversationStructMapper::toDTO);
    }

    @Override
    public ChatConversationDTO getConversationDetail(Long conversationId, Long userId) {
        ChatConversationDO conversation = chatConversationDAO.getByIdAndUserId(conversationId, userId);
        if (conversation == null) {
            ResultCode.AI_CONVERSATION_NOT_EXISTS.throwException();
        }
        return chatConversationStructMapper.toDTO(conversation);
    }

    @Override
    public PageVO<ChatMessageDTO> pageQueryMessages(Long conversationId, Long userId, MessagesQueryParam queryParam) {
        ChatConversationDO conversation = chatConversationDAO.getByIdAndUserId(conversationId, userId);
        if (conversation == null) {
            ResultCode.AI_CONVERSATION_NOT_EXISTS.throwException();
        }

        IPage<ChatMessageDO> pageQuery = PageUtils.of(queryParam);
        IPage<ChatMessageDO> messagePage = chatMessageDAO.pageQueryMessages(conversationId, pageQuery, queryParam.getBeforeTime());
        return PageUtils.from(messagePage, chatMessageStructMapper::toDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long conversationId, Long userId) {
        log.info("删除对话，conversationId: {}, userId: {}", conversationId, userId);

        validateConversationAccess(conversationId, userId);
        boolean deleted = chatConversationDAO.deleteConversation(conversationId, userId);
        if (deleted) {
            chatMessageDAO.deleteMessagesByConversationId(conversationId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveConversation(Long conversationId, Long userId) {
        log.info("归档对话，conversationId: {}, userId: {}", conversationId, userId);

        validateConversationAccess(conversationId, userId);
        chatConversationDAO.archiveConversation(conversationId, userId);
    }

    private Long getCurrentUserId() {
        Long userId = ReqInfoContext.getContext().getUserId();
        if (userId == null) {
            ResultCode.AUTHENTICATION_FAILED.throwException();
        }
        return userId;
    }

    /**
     * 验证聊天请求
     */
    private void validateChatRequest(String message, Long userId) {
        if (message.length() > aiLimitConfig.getMaxMessageLength()) {
            ResultCode.AI_MESSAGE_TOO_LONG.throwException();
        }

        if (!chatUsageService.checkHourlyMessageLimit(userId)) {
            ResultCode.AI_HOURLY_LIMIT_EXCEEDED.throwException();
        }

        if (!chatUsageService.checkDailyLimit(userId)) {
            ResultCode.AI_DAILY_LIMIT_EXCEEDED.throwException();
        }
    }

    /**
     * 准备对话（创建或验证）
     */
    private Long prepareConversation(Long conversationId, Long userId, String message) {
        if (conversationId == null) {
            // 新对话：让AI生成标题
            String title = generateConversationTitle(message);
            conversationId = chatConversationDAO.createConversation(userId, title);
            log.info("创建新对话，conversationId: {}, title: {}", conversationId, title);
            return conversationId;
        }

        // 验证现有对话的权限
        ChatConversationDO conversation = chatConversationDAO.getByIdAndUserId(conversationId, userId);
        if (conversation == null) {
            ResultCode.AI_CONVERSATION_NOT_EXISTS.throwException();
        }
        return conversationId;
    }

    /**
     * 生成对话标题
     */
    private String generateConversationTitle(String message) {
        try {
            // 使用Spring AI推荐的ChatClient fluent API生成标题
            String title = deepseekChatClient.prompt()
                    .system("你是一个标题生成助手，请为用户问题生成一个简洁的标题。要求：1）不超过20个字符 2）不包含引号 3）直接返回标题内容")
                    .user("请为以下问题生成标题：" + message)
                    .options(OpenAiChatOptions.builder().withModel("deepseek-chat").build())
                    .call()
                    .content();

            if (StringUtils.hasText(title)) {
                // 清理AI返回的标题（去除可能的引号、换行等）
                title = title.trim()
                        .replaceAll("^[\"'《]|[\"'》]$", "")
                        .replaceAll("\n\r", "");

                if (title.length() <= 20) {
                    return title;
                }
                // 如果标题太长，截取前20个字符
                return title.substring(0, 20) + "...";
            }
        } catch (Exception e) {
            log.warn("AI生成标题失败，使用默认方式: {}", e.getMessage());
        }

        // 降级方案：简单截取
        return message.length() <= 20 ? message : message.substring(0, 20) + "...";
    }

    /**
     * 保存用户消息
     */
    private boolean saveUserMessage(Long conversationId, Long userId, String message) {
        ChatMessageDO userMessage = new ChatMessageDO();
        userMessage.setConversationId(conversationId);
        userMessage.setUserId(userId);
        userMessage.setMessageType(ChatMessageRoleEnum.USER);
        userMessage.setContent(message);
        userMessage.setProvider(null);
        userMessage.setModelName("");

        return chatMessageDAO.save(userMessage);
    }

    /**
     * 执行AI聊天 - 按照Spring AI最佳实践实现多厂商动态选择
     */
    private ChatResult executeChat(Long conversationId, String userMessage, ChatReq chatReq) {
        // 1. 根据前端参数选择ChatClient
        ChatClient selectedClient = selectChatClient(chatReq.getProvider());

        // 2. 构建ChatOptions
        OpenAiChatOptions chatOptions = buildChatOptions(chatReq);

        // 3. 获取最近10条消息作为上下文
        List<ChatMessageDO> recentMessages = chatMessageDAO.getRecentMessages(conversationId, 10);

        try {
            // 4. 使用Spring AI推荐的方式构建消息列表，包含历史上下文
            List<Message> messages = new ArrayList<>();

            // 添加系统消息
            messages.add(new SystemMessage("你是一个有用的AI助手，请用中文回答问题。"));

            // 5. 添加历史消息作为上下文
            if (!CollectionUtils.isEmpty(recentMessages)) {
                for (int i = recentMessages.size() - 1; i >= 0; i--) {
                    ChatMessageDO msg = recentMessages.get(i);
                    if (msg.getMessageType() == ChatMessageRoleEnum.USER) {
                        messages.add(new UserMessage(msg.getContent()));
                    } else if (msg.getMessageType() == ChatMessageRoleEnum.ASSISTANT) {
                        messages.add(new AssistantMessage(msg.getContent()));
                    }
                }
            }

            // 6. 添加当前用户消息
            messages.add(new UserMessage(userMessage));

            // 7. 构建 Prompt 并应用配置选项
            Prompt prompt = new Prompt(messages, chatOptions);

            // 8. 执行调用并获取完整响应
            ChatResponse response = selectedClient.prompt(prompt).call().chatResponse();

            String content = response.getResult().getOutput().getContent();
            if (!StringUtils.hasText(content)) {
                ResultCode.AI_RESPONSE_EMPTY.throwException();
            }

            // 9. 构建结果对象
            return getChatResult(chatReq, content, response);
        } catch (Exception e) {
            log.error("AI调用失败，Provider: {}, Error: {} - {}",
                    chatReq.getProvider(), e.getClass().getSimpleName(), e.getMessage(), e);

            // 根据异常类型提供不同的错误信息
            if (e.getMessage() != null && e.getMessage().contains("rate limit")) {
                ResultCode.AI_RESPONSE_EMPTY.throwException("AI服务频率限制，请稍后重试");
            } else if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                ResultCode.AI_RESPONSE_EMPTY.throwException("AI服务响应超时，请稍后重试");
            } else {
                ResultCode.AI_RESPONSE_EMPTY.throwException("AI服务暂时不可用");
            }
            return null;
        }
    }

    private static ChatResult getChatResult(ChatReq chatReq, String content, ChatResponse response) {
        ChatResult result = new ChatResult();
        result.setContent(content);
        result.setProvider(chatReq.getProvider());

        // 提取Token使用量和模型信息
        if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
            var usage = response.getMetadata().getUsage();
            result.setPromptTokens(usage.getPromptTokens());
            result.setCompletionTokens(usage.getGenerationTokens());
            result.setTotalTokens(usage.getTotalTokens());
        }

        // 获取模型信息
        if (response.getMetadata() != null) {
            result.setModel(response.getMetadata().getModel());
        }
        return result;
    }

    /**
     * 根据provider参数选择对应的ChatClient
     */
    private ChatClient selectChatClient(ChatClientTypeEnum provider) {
        if (provider == null) {
            return deepseekChatClient;
        }

        return switch (provider) {
            case ChatClientTypeEnum.DEEPSEEK -> deepseekChatClient;
            case ChatClientTypeEnum.QWEN -> qwenChatClient;
            default -> deepseekChatClient;
        };
    }

    /**
     * 根据前端参数构建ChatOptions
     */
    private OpenAiChatOptions buildChatOptions(ChatReq chatReq) {
        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder();
        boolean hasOptions = false;

        if (StringUtils.hasText(chatReq.getModel())) {
            builder.withModel(chatReq.getModel());
            hasOptions = true;
        }

        if (chatReq.getTemperature() != null) {
            builder.withTemperature(chatReq.getTemperature().doubleValue());
            hasOptions = true;
        }

        if (chatReq.getMaxTokens() != null) {
            builder.withMaxTokens(chatReq.getMaxTokens());
            hasOptions = true;
        }

        return hasOptions ? builder.build() : null;
    }

    /**
     * 保存AI回复 - 使用Spring AI提供的完整信息
     */
    private ChatMessageDTO saveChatResponse(Long conversationId, Long userId, ChatResult chatResult, boolean isNewConversation) {
        // 保存AI消息，使用从Spring AI获取的真实信息
        ChatMessageDO chatMessage = getChatMessageDO(conversationId, userId, chatResult);

        chatMessageDAO.save(chatMessage);

        // 记录使用量统计
        Integer conversationIncrement = isNewConversation ? 1 : 0;
        chatUsageService.recordUsage(userId, chatResult.getProvider(), chatResult.getModel(),
                2, chatResult.getPromptTokens(), chatResult.getCompletionTokens(),
                chatResult.getTotalTokens(), conversationIncrement);

        log.info("AI对话完成，conversationId: {}, provider: {}, model: {}",
                conversationId, chatResult.getProvider(), chatResult.getModel());
        return chatMessageStructMapper.toDTO(chatMessage);
    }

    private static ChatMessageDO getChatMessageDO(Long conversationId, Long userId, ChatResult chatResult) {
        ChatMessageDO chatMessage = new ChatMessageDO();
        chatMessage.setConversationId(conversationId);
        chatMessage.setUserId(userId);
        chatMessage.setMessageType(ChatMessageRoleEnum.ASSISTANT);
        chatMessage.setContent(chatResult.getContent());
        chatMessage.setProvider(chatResult.getProvider());
        chatMessage.setModelName(chatResult.getModel() != null ? chatResult.getModel() : "unknown");
        chatMessage.setPromptTokens(chatResult.getPromptTokens());
        chatMessage.setCompletionTokens(chatResult.getCompletionTokens());
        chatMessage.setTotalTokens(chatResult.getTotalTokens());
        return chatMessage;
    }

    /**
     * 验证对话访问权限
     */
    private void validateConversationAccess(Long conversationId, Long userId) {
        ChatConversationDO conversation = chatConversationDAO.getByIdAndUserId(conversationId, userId);
        if (conversation == null) {
            ResultCode.AI_CONVERSATION_NOT_EXISTS.throwException();
        }
    }
}