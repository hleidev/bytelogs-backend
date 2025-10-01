package top.harrylei.community.service.ai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.type.TypeReference;
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
import top.harrylei.community.api.enums.ai.ChatClientTypeEnum;
import top.harrylei.community.api.enums.ai.ChatConversationStatusEnum;
import top.harrylei.community.api.enums.ai.ChatMessageRoleEnum;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.model.ai.dto.ChatConversationDTO;
import top.harrylei.community.api.model.ai.dto.ChatMessageDTO;
import top.harrylei.community.api.model.ai.req.ChatReq;
import top.harrylei.community.api.model.ai.req.ConversationsQueryParam;
import top.harrylei.community.api.model.ai.req.MessagesQueryParam;
import top.harrylei.community.api.model.ai.vo.ChatResult;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.core.common.constans.RedisKeyConstants;
import top.harrylei.community.core.config.AILimitConfig;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.PageUtils;
import top.harrylei.community.core.util.RedisUtil;
import top.harrylei.community.service.ai.adapter.ChatOptionsAdapter;
import top.harrylei.community.service.ai.config.AiProviderConfig;
import top.harrylei.community.service.ai.converted.ChatConversationStructMapper;
import top.harrylei.community.service.ai.converted.ChatMessageStructMapper;
import top.harrylei.community.service.ai.repository.dao.ChatConversationDAO;
import top.harrylei.community.service.ai.repository.dao.ChatMessageDAO;
import top.harrylei.community.service.ai.repository.entity.ChatConversationDO;
import top.harrylei.community.service.ai.repository.entity.ChatMessageDO;
import top.harrylei.community.service.ai.service.ChatService;
import top.harrylei.community.service.ai.service.ChatUsageService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
    private final ChatOptionsAdapter chatOptionsAdapter;
    private final AiProviderConfig aiProviderConfig;
    private final RedisUtil redisUtil;

    @Override
    public void chatStream(ChatReq chatReq, StreamCallback streamCallback) {
        Long userId = getCurrentUserId();
        String userMessage = chatReq.getMessage();

        log.info("用户发起AI流式对话，userId: {}, conversationId: {}, message长度: {}",
                userId, chatReq.getConversationId(), userMessage.length());

        try {
            // 1. 验证请求
            validateChatRequest(userMessage, userId);

            // 2. 准备对话
            boolean isNewConversation = (chatReq.getConversationId() == null);
            Long conversationId = prepareConversation(chatReq.getConversationId(), userId, userMessage);

            // 3. 保存用户消息
            saveUserMessage(conversationId, userId, userMessage);

            // 4. 执行流式聊天
            executeChatStream(conversationId, userMessage, chatReq, streamCallback, userId, isNewConversation);

        } catch (Exception e) {
            log.error("AI流式对话失败，userId: {}, error: {}", userId, e.getMessage(), e);
            Long conversationId = chatReq.getConversationId();
            streamCallback.onError(conversationId, e.getMessage());
        }
    }

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

        boolean deleted = chatConversationDAO.delete(conversationId, userId);
        if (deleted) {
            chatMessageDAO.deleteByConversationId(conversationId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveConversation(Long conversationId, Long userId) {
        log.info("归档对话，conversationId: {}, userId: {}", conversationId, userId);

        chatConversationDAO.archive(conversationId, userId);
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
            conversationId = chatConversationDAO.create(userId, title);
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
            // 使用配置化的方式生成标题，避免硬编码
            ChatReq titleReq = new ChatReq();
            ChatClientTypeEnum titleProvider = aiProviderConfig.getDefaultProvider();
            titleReq.setProvider(titleProvider);
            titleReq.setTemperature(0.3f);
            titleReq.setMaxTokens(50);

            Object titleOptions = chatOptionsAdapter.buildChatOptions(titleReq, titleProvider);

            // 根据配置的默认提供商选择ChatClient生成标题
            ChatClient titleClient = selectChatClient(titleProvider);

            String title;
            if (titleOptions instanceof OpenAiChatOptions options) {
                title = titleClient.prompt()
                        .system("你是一个标题生成助手，请为用户问题生成一个简洁的标题。要求：1）不超过20个字符 2）不包含引号 3）直接返回标题内容")
                        .user("请为以下问题生成标题：" + message)
                        .options(options)
                        .call()
                        .content();
            } else {
                title = titleClient.prompt()
                        .system("你是一个标题生成助手，请为用户问题生成一个简洁的标题。要求：1）不超过20个字符 2）不包含引号 3）直接返回标题内容")
                        .user("请为以下问题生成标题：" + message)
                        .call()
                        .content();
            }

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

        boolean saved = chatMessageDAO.save(userMessage);

        // 保存用户消息后立即更新缓存
        if (saved) {
            updateMessagesContext(conversationId, userMessage);
        }

        return saved;
    }

    /**
     * 执行AI聊天 - 按照Spring AI最佳实践实现多厂商动态选择
     */
    private ChatResult executeChat(Long conversationId, String userMessage, ChatReq chatReq) {
        // 1. 根据前端参数选择ChatClient
        ChatClient selectedClient = selectChatClient(chatReq.getProvider());

        // 2. 构建ChatOptions - 使用适配器解决硬编码问题
        Object chatOptions = chatOptionsAdapter.buildChatOptions(chatReq, chatReq.getProvider());

        // 3. 获取最近10条消息作为上下文（带缓存）
        List<ChatMessageDO> recentMessages = getMessagesContext(conversationId, 10);

        try {
            // 4. 使用Spring AI推荐的方式构建消息列表，包含历史上下文
            Prompt prompt = buildPrompt(userMessage, recentMessages, chatOptions);

            // 8. 执行调用并获取完整响应
            ChatResponse response = selectedClient.prompt(prompt).call().chatResponse();

            String content = response.getResult().getOutput().getContent();
            if (!StringUtils.hasText(content)) {
                ResultCode.AI_RESPONSE_EMPTY.throwException();
            }

            // 9. 构建结果对象
            return getChatResult(chatReq, response);
        } catch (Exception e) {
            log.error("AI调用失败，Provider: {}, ConversationId: {}, Error: {} - {}, Cause: {}",
                    chatReq.getProvider(), conversationId, e.getClass().getSimpleName(), e.getMessage(),
                    e.getCause() != null ? e.getCause().getMessage() : "无具体原因", e);

            // 根据异常类型提供不同的错误信息
            if (e.getMessage() != null && e.getMessage().contains("rate limit")) {
                ResultCode.AI_RESPONSE_EMPTY.throwException("AI服务频率限制，请稍后重试");
            } else if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                ResultCode.AI_RESPONSE_EMPTY.throwException("AI服务响应超时，请稍后重试");
            } else {
                ResultCode.AI_RESPONSE_EMPTY.throwException("AI服务暂时不可用: " + e.getMessage());
            }
            return null;
        }
    }

    private static ChatResult getChatResult(ChatReq chatReq, ChatResponse response) {
        ChatResult result = new ChatResult();
        result.setContent(response.getResult().getOutput().getContent());
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
            provider = aiProviderConfig.getDefaultProvider();
        }

        return switch (provider) {
            case DEEPSEEK -> deepseekChatClient;
            case QWEN -> qwenChatClient;
            case OPENAI -> throw new IllegalStateException("OpenAI ChatClient未配置，请完成相关配置");
        };
    }

    /**
     * 保存AI回复 - 使用Spring AI提供的完整信息
     */
    private ChatMessageDTO saveChatResponse(Long conversationId, Long userId, ChatResult chatResult, boolean isNewConversation) {
        // 保存AI消息，使用从Spring AI获取的真实信息
        ChatMessageDO chatMessage = buildChatMessageDO(conversationId, userId, chatResult);

        chatMessageDAO.save(chatMessage);

        // 更新上下文缓存
        updateMessagesContext(conversationId, chatMessage);

        // 记录使用量统计
        Integer conversationIncrement = isNewConversation ? 1 : 0;
        chatUsageService.recordUsage(userId, chatResult.getProvider(), chatResult.getModel(),
                2, chatResult.getPromptTokens(), chatResult.getCompletionTokens(),
                chatResult.getTotalTokens(), conversationIncrement);

        log.info("AI对话完成，conversationId: {}, provider: {}, model: {}",
                conversationId, chatResult.getProvider(), chatResult.getModel());
        return chatMessageStructMapper.toDTO(chatMessage);
    }

    private static ChatMessageDO buildChatMessageDO(Long conversationId, Long userId, ChatResult chatResult) {
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
     * 执行流式AI聊天
     */
    private void executeChatStream(Long conversationId, String userMessage, ChatReq chatReq,
                                   StreamCallback streamCallback, Long userId, boolean isNewConversation) {
        // 1. 根据前端参数选择ChatClient
        ChatClient chatClient = selectChatClient(chatReq.getProvider());

        // 2. 构建ChatOptions
        Object chatOptions = chatOptionsAdapter.buildChatOptions(chatReq, chatReq.getProvider());

        // 3. 获取最近10条消息作为上下文（带缓存）
        List<ChatMessageDO> recentMessages = getMessagesContext(conversationId, 10);

        try {
            // 4. 构建消息列表
            Prompt prompt = buildPrompt(userMessage, recentMessages, chatOptions);

            // 5. 准备保存消息的ID（预分配，避免流式过程中的数据库操作）
            // 使用时间戳作为临时ID，后续保存时会获取真实ID
            Long messageId = System.currentTimeMillis();
            StringBuilder fullContent = new StringBuilder();
            // 用于存储从响应中提取的模型名称和最终token统计
            final String[] modelName = {null};
            final AtomicReference<Long> finalTokens = new AtomicReference<>(0L);
            final AtomicReference<Long> finalPromptTokens = new AtomicReference<>(0L);
            final AtomicReference<Long> finalCompletionTokens = new AtomicReference<>(0L);
            final AtomicReference<Object> finalMetadata = new AtomicReference<>();
            final long[] startTimeRef = {System.currentTimeMillis()};

            // 6. 执行流式调用（添加背压处理）
            chatClient.prompt(prompt).stream().chatResponse()
                    // 缓冲最多100个响应片段
                    .onBackpressureBuffer(100)
                    .doOnNext(chatResponse -> handleStreamingChunk(conversationId, streamCallback,
                            chatResponse, fullContent, messageId, modelName, finalMetadata))
                    .doOnComplete(() -> {
                        handleStreamComplete(conversationId, chatReq, streamCallback, userId, isNewConversation,
                                finalMetadata, finalTokens, finalPromptTokens, finalCompletionTokens,
                                fullContent, modelName, startTimeRef);
                    })
                    .doOnError(error -> {
                        log.error("AI流式调用失败，conversationId: {}, error: {}", conversationId, error.getMessage(), error);
                        streamCallback.onError(conversationId, "AI服务暂时不可用: " + error.getMessage());
                    })
                    .subscribe(); // 订阅开始流式处理

        } catch (Exception e) {
            log.error("AI流式对话执行失败，conversationId: {}, error: {}", conversationId, e.getMessage(), e);
            streamCallback.onError(conversationId, "AI服务异常: " + e.getMessage());
        }
    }

    private void handleStreamComplete(Long conversationId, ChatReq chatReq, StreamCallback streamCallback, Long userId,
                                      boolean isNewConversation, AtomicReference<Object> finalMetadata,
                                      AtomicReference<Long> finalTokens, AtomicReference<Long> finalPromptTokens,
                                      AtomicReference<Long> finalCompletionTokens, StringBuilder fullContent,
                                      String[] modelName, long[] startTimeRef) {
        // 流式响应完成，从最终metadata中获取准确的token统计
        Object metadata = finalMetadata.get();
        if (metadata != null) {
            try {
                // 通过反射获取usage统计信息，避免强类型依赖
                Object usage = metadata.getClass().getMethod("getUsage").invoke(metadata);
                if (usage != null) {
                    // 获取各类token统计
                    Long totalTokens = (Long) usage.getClass().getMethod("getTotalTokens").invoke(usage);
                    Long promptTokens = (Long) usage.getClass().getMethod("getPromptTokens").invoke(usage);
                    Long completionTokens = (Long) usage.getClass().getMethod("getGenerationTokens").invoke(usage);

                    log.info("流式响应完成时的token统计 - total: {}, prompt: {}, completion: {}",
                            totalTokens, promptTokens, completionTokens);

                    if (totalTokens != null && totalTokens > 0) {
                        finalTokens.set(totalTokens);
                    }
                    if (promptTokens != null && promptTokens > 0) {
                        finalPromptTokens.set(promptTokens);
                    }
                    if (completionTokens != null && completionTokens > 0) {
                        finalCompletionTokens.set(completionTokens);
                    }
                }
            } catch (Exception e) {
                log.debug("无法获取token统计详情: {}, 使用内容长度作为近似值", e.getMessage());
            }
        }

        // 直接使用从API返回的token统计
        Long actualTotalTokens = finalTokens.get();
        Long actualPromptTokens = finalPromptTokens.get();
        Long actualCompletionTokens = finalCompletionTokens.get();

        // 构建包含token信息的ChatResult
        ChatResult chatResult = ChatResult.builder()
                .content(fullContent.toString())
                .provider(chatReq.getProvider())
                .model(modelName[0] != null ? modelName[0] : "undefined")
                .promptTokens(actualPromptTokens)
                .completionTokens(actualCompletionTokens)
                .totalTokens(actualTotalTokens)
                .build();
        // 保存AI回复到数据库
        ChatMessageDO chatMessage = buildChatMessageDO(conversationId, userId, chatResult);
        chatMessageDAO.save(chatMessage);

        // 更新上下文缓存
        updateMessagesContext(conversationId, chatMessage);

        // 记录使用量统计
        Integer conversationIncrement = isNewConversation ? 1 : 0;

        chatUsageService.recordUsage(userId, chatResult.getProvider(), chatResult.getModel(),
                2, actualPromptTokens, actualCompletionTokens, actualTotalTokens, conversationIncrement);

        // 通知完成 - 传递实际token数给前端
        Integer tokenCountForCallback = actualTotalTokens > 0 ? actualTotalTokens.intValue() : 0;
        streamCallback.onComplete(conversationId, chatMessage.getId(), tokenCountForCallback);

        long endTime = System.currentTimeMillis();
        log.info("AI流式对话完成，conversationId: {}, messageId: {}, contentLength: {}, totalTokens: {}, promptTokens: {}, completionTokens: {}, 耗时: {}ms",
                conversationId, chatMessage.getId(), fullContent.length(), actualTotalTokens, actualPromptTokens, actualCompletionTokens, endTime - startTimeRef[0]);
    }

    private static void handleStreamingChunk(Long conversationId, StreamCallback streamCallback,
                                             ChatResponse chatResponse, StringBuilder fullContent,
                                             Long messageId, String[] modelName, AtomicReference<Object> finalMetadata) {
        // 处理每个流式响应片段
        String content = chatResponse.getResult().getOutput().getContent();
        if (StringUtils.hasText(content)) {
            fullContent.append(content);
            streamCallback.onContent(conversationId, messageId, content);
        }

        // 提取模型信息（首次响应时）
        if (modelName[0] == null && chatResponse.getMetadata() != null) {
            modelName[0] = chatResponse.getMetadata().getModel();
        }

        // 持续更新metadata，以获取最终的token统计
        if (chatResponse.getMetadata() != null) {
            finalMetadata.set(chatResponse.getMetadata());
        }
    }

    private static Prompt buildPrompt(String userMessage, List<ChatMessageDO> recentMessages, Object chatOptions) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个牛逼的AI助手，请用中文回答问题。"));

        // 5. 添加历史消息作为上下文（recentMessages已经是时间正序）
        if (!CollectionUtils.isEmpty(recentMessages)) {
            for (ChatMessageDO msg : recentMessages) {
                if (msg.getMessageType() == ChatMessageRoleEnum.USER) {
                    messages.add(new UserMessage(msg.getContent()));
                } else if (msg.getMessageType() == ChatMessageRoleEnum.ASSISTANT) {
                    messages.add(new AssistantMessage(msg.getContent()));
                }
            }
        }

        // 6. 添加当前用户消息
        messages.add(new UserMessage(userMessage));

        // 7. 构建Prompt并应用配置选项
        Prompt prompt;
        if (chatOptions instanceof OpenAiChatOptions options) {
            prompt = new Prompt(messages, options);
        } else {
            prompt = new Prompt(messages);
        }
        return prompt;
    }

    /**
     * 获取最近消息（带缓存）
     */
    private List<ChatMessageDO> getMessagesContext(Long conversationId, Integer limit) {
        String cacheKey = RedisKeyConstants.getChatContextKey(conversationId);

        try {
            LinkedList<ChatMessageDO> messageQueue = redisUtil.get(cacheKey, new TypeReference<>() {
            });
            if (messageQueue != null && !messageQueue.isEmpty()) {
                return new ArrayList<>(messageQueue);
            }
        } catch (Exception e) {
            log.warn("读取上下文缓存失败: conversationId={}, error={}", conversationId, e.getMessage());
            try {
                redisUtil.del(cacheKey);
            } catch (Exception deleteException) {
                log.warn("删除损坏缓存失败: {}", deleteException.getMessage());
            }
        }

        // 缓存未命中，重建缓存队列
        rebuildMessageQueue(conversationId, limit);

        // 重新获取
        try {
            LinkedList<ChatMessageDO> messageQueue = redisUtil.get(cacheKey, new TypeReference<>() {
            });
            return messageQueue != null ? new ArrayList<>(messageQueue) : new ArrayList<>();
        } catch (Exception e) {
            log.warn("重新获取缓存失败: conversationId={}, error={}", conversationId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 增量更新对话上下文缓存
     */
    private void updateMessagesContext(Long conversationId, ChatMessageDO newMessage) {
        String cacheKey = RedisKeyConstants.getChatContextKey(conversationId);

        try {
            LinkedList<ChatMessageDO> messageQueue = redisUtil.get(cacheKey, new TypeReference<>() {
            });

            if (messageQueue == null) {
                messageQueue = new LinkedList<>();
            }

            // 直接添加到队尾（保持时间顺序）
            messageQueue.addLast(newMessage);

            // 保持队列大小限制：超出10条时移除队头
            while (messageQueue.size() > 10) {
                messageQueue.removeFirst();
            }

            // 更新缓存
            redisUtil.set(cacheKey, messageQueue, Duration.ofMinutes(30));

        } catch (Exception e) {
            log.warn("更新上下文缓存失败: conversationId={}, error={}", conversationId, e.getMessage());
        }
    }

    /**
     * 重建消息队列缓存
     */
    private void rebuildMessageQueue(Long conversationId, Integer limit) {
        String cacheKey = RedisKeyConstants.getChatContextKey(conversationId);

        try {
            // 查询数据库获取最近消息
            List<ChatMessageDO> dbMessages = chatMessageDAO.getRecentMessages(conversationId, limit);

            // 构建有序队列：DAO返回倒序，需要转为正序队列
            LinkedList<ChatMessageDO> messageQueue = new LinkedList<>();
            for (int i = dbMessages.size() - 1; i >= 0; i--) {
                messageQueue.addLast(dbMessages.get(i));
            }

            // 写入缓存
            if (!messageQueue.isEmpty()) {
                redisUtil.set(cacheKey, messageQueue, Duration.ofMinutes(30));
            }

        } catch (Exception e) {
            log.warn("重建上下文缓存失败: conversationId={}, error={}", conversationId, e.getMessage());
        }
    }
}