package top.harrylei.community.web.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.harrylei.community.api.enums.ai.ChatClientTypeEnum;
import top.harrylei.community.api.enums.ai.ChatConversationStatusEnum;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.model.ai.dto.ChatConversationDTO;
import top.harrylei.community.api.model.ai.dto.ChatMessageDTO;
import top.harrylei.community.api.model.ai.dto.ChatUsageStatsDTO;
import top.harrylei.community.api.model.ai.req.ChatReq;
import top.harrylei.community.api.model.ai.req.ConversationsQueryParam;
import top.harrylei.community.api.model.ai.req.MessagesQueryParam;
import top.harrylei.community.api.model.ai.vo.*;
import top.harrylei.community.api.model.base.Result;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.core.config.AILimitConfig;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.security.permission.RequiresLogin;
import top.harrylei.community.core.util.NumUtil;
import top.harrylei.community.core.util.PageUtils;
import top.harrylei.community.service.ai.config.AiProviderConfig;
import top.harrylei.community.service.ai.converted.ChatConversationStructMapper;
import top.harrylei.community.service.ai.converted.ChatMessageStructMapper;
import top.harrylei.community.service.ai.service.ChatService;
import top.harrylei.community.service.ai.service.ChatUsageService;
import top.harrylei.community.web.websocket.WebSocketSessionManager;
import top.harrylei.community.api.model.websocket.message.AiStreamMessage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * AI对话控制器
 *
 * @author harry
 */
@RestController
@RequestMapping("/v1/ai/chat")
@Tag(name = "AI对话模块", description = "AI对话相关接口")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatUsageService chatUsageService;
    private final AILimitConfig aiLimitConfig;
    private final AiProviderConfig aiProviderConfig;
    private final ChatConversationStructMapper chatConversationStructMapper;
    private final ChatMessageStructMapper chatMessageStructMapper;
    private final WebSocketSessionManager webSocketSessionManager;

    @RequiresLogin
    @PostMapping
    @Operation(summary = "发起AI对话", description = "发送消息给AI并获取回复")
    public Result<ChatMessageVO> chat(@Valid @RequestBody ChatReq req) {
        ChatMessageDTO aiMessage = chatService.chat(req);
        ChatMessageVO messageVO = chatMessageStructMapper.toVO(aiMessage);
        return Result.success(messageVO);
    }

    @RequiresLogin
    @PostMapping("/stream")
    @Operation(summary = "发起AI流式对话", description = "通过WebSocket发送流式AI响应")
    public Result<Void> chatStream(@Valid @RequestBody ChatReq req) {
        Long userId = getCurrentUserId();

        // 检查用户是否在线
        if (!webSocketSessionManager.isUserOnline(userId)) {
            ResultCode.INVALID_PARAMETER.throwException("用户未连接WebSocket，无法进行流式对话");
        }

        // 使用回调接口处理流式响应
        chatService.chatStream(req, new ChatService.StreamCallback() {
            @Override
            public void onContent(Long conversationId, Long messageId, String content) {
                // 发送流式内容片段到WebSocket
                AiStreamMessage message = AiStreamMessage.chunk(conversationId, messageId, content);
                webSocketSessionManager.sendAiStream(userId, message);
            }

            @Override
            public void onComplete(Long conversationId, Long messageId, Long promptTokens, Long completionTokens, Long totalTokens) {
                // 发送完成消息（包含完整的 token 统计）
                AiStreamMessage message = AiStreamMessage.finish(conversationId, messageId, promptTokens, completionTokens, totalTokens);
                webSocketSessionManager.sendAiStream(userId, message);
            }

            @Override
            public void onError(Long conversationId, String error) {
                // 发送错误消息
                AiStreamMessage message = AiStreamMessage.error(conversationId, error);
                webSocketSessionManager.sendAiStream(userId, message);
            }
        });

        return Result.success();
    }

    @RequiresLogin
    @GetMapping("/conversations/page")
    @Operation(summary = "获取对话列表", description = "获取当前用户的对话列表（进行中）")
    public Result<PageVO<ChatConversationVO>> pageQuery(@Valid ConversationsQueryParam queryParam) {
        Long userId = getCurrentUserId();
        PageVO<ChatConversationDTO> conversationPage = chatService.pageQueryConversations(userId, queryParam, ChatConversationStatusEnum.ACTIVE);
        return Result.success(PageUtils.map(conversationPage, chatConversationStructMapper::toVO));
    }

    @RequiresLogin
    @GetMapping("/conversations/archived/page")
    @Operation(summary = "获取归档对话列表", description = "获取当前用户的归档对话列表")
    public Result<PageVO<ChatConversationVO>> pageQueryArchived(@Valid ConversationsQueryParam queryParam) {
        Long userId = getCurrentUserId();
        PageVO<ChatConversationDTO> conversationPage = chatService.pageQueryConversations(userId, queryParam, ChatConversationStatusEnum.ARCHIVED);
        return Result.success(PageUtils.map(conversationPage, chatConversationStructMapper::toVO));
    }

    @RequiresLogin
    @GetMapping("/conversations/{conversionId}")
    @Operation(summary = "获取对话详情", description = "获取指定对话的详细信息和消息历史")
    public Result<ChatConversationDetailVO> getConversationDetail(@NotNull(message = "会话ID不能为空")
                                                                  @PathVariable Long conversionId) {
        Long userId = getCurrentUserId();
        ChatConversationDTO conversation = chatService.getConversationDetail(conversionId, userId);

        // 获取最新的消息列表（首屏显示）
        MessagesQueryParam queryParam = new MessagesQueryParam();
        queryParam.setPageNum(1);
        queryParam.setPageSize(20);
        PageVO<ChatMessageDTO> messagePage = chatService.pageQueryMessages(conversionId, userId, queryParam);

        // 转换DTO为VO
        ChatConversationDetailVO pageResult = chatConversationStructMapper.toDetailVO(conversation);
        pageResult.setMessages(PageUtils.map(messagePage, chatMessageStructMapper::toVO).getContent());

        return Result.success(pageResult);
    }

    @RequiresLogin
    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "获取对话消息", description = "支持基于时间游标的滚动加载")
    public Result<PageVO<ChatMessageVO>> pageQueryMessages(@NotNull(message = "会话ID不能为空") @PathVariable Long id,
                                                           @Valid MessagesQueryParam queryParam) {
        Long userId = getCurrentUserId();
        PageVO<ChatMessageDTO> messagePage = chatService.pageQueryMessages(id, userId, queryParam);

        // 使用PageUtils进行DTO到VO的转换
        return Result.success(PageUtils.map(messagePage, chatMessageStructMapper::toVO));
    }

    @RequiresLogin
    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "删除对话", description = "删除指定的对话")
    public Result<Void> deleteConversation(@NotNull(message = "对话ID不能为空") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        chatService.deleteConversation(id, userId);

        return Result.success();
    }

    @RequiresLogin
    @PutMapping("/conversations/{id}/archive")
    @Operation(summary = "归档对话", description = "归档指定的对话")
    public Result<Void> archiveConversation(@NotNull(message = "对话ID不能为空") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        chatService.archiveConversation(id, userId);

        return Result.success();
    }

    private static Long getCurrentUserId() {
        Long userId = ReqInfoContext.getContext().getUserId();
        if (!NumUtil.upZero(userId)) {
            ResultCode.INTERNAL_ERROR.throwException();
        }
        return userId;
    }

    @RequiresLogin
    @GetMapping("/usage")
    @Operation(summary = "获取使用量统计", description = "获取当前用户的AI使用量统计")
    public Result<ChatUsageStatsVO> getUsageStats() {
        Long userId = getCurrentUserId();
        LocalDate today = LocalDate.now();

        ChatUsageStatsDTO todayUsage = chatUsageService.getDailyUsage(userId, today);

        ChatUsageStatsVO result = new ChatUsageStatsVO();
        result.setDate(today);

        if (todayUsage != null) {
            result.setMessageCount(todayUsage.getMessageCount());
            result.setTokensUsed(todayUsage.getTotalTokens());
            result.setConversationCount(todayUsage.getConversationCount());
        } else {
            result.setMessageCount(0);
            result.setTokensUsed(0L);
            result.setConversationCount(0);
        }

        // 设置限制和剩余量
        result.setDailyMessageLimit(aiLimitConfig.getDailyMessageLimit());
        result.setDailyTokenLimit(aiLimitConfig.getDailyTokenLimit());
        result.setRemainingMessages(chatUsageService.getRemainingMessages(userId));

        // 计算剩余Token，避免负数
        long remainingTokens = Math.max(0, aiLimitConfig.getDailyTokenLimit() - result.getTokensUsed());
        result.setRemainingTokens(remainingTokens);

        return Result.success(result);
    }

    @GetMapping("/providers")
    @Operation(summary = "获取AI提供商信息", description = "获取所有可用的AI提供商及其支持的模型信息")
    public Result<List<AiProviderInfoVO>> getProviders() {
        List<AiProviderInfoVO> providers = new ArrayList<>();

        // 遍历所有支持的提供商类型
        for (ChatClientTypeEnum provider : ChatClientTypeEnum.values()) {
            if (aiProviderConfig.isProviderEnabled(provider)) {
                AiProviderInfoVO providerInfo = AiProviderInfoVO.builder()
                        .provider(provider)
                        .enabled(true)
                        .defaultModel(aiProviderConfig.getDefaultModel(provider))
                        .supportedModels(aiProviderConfig.getSupportedModels(provider))
                        .temperatureRange(aiProviderConfig.getTemperatureRange(provider))
                        .maxTokens(aiProviderConfig.getMaxTokens(provider))
                        .build();
                providers.add(providerInfo);
            }
        }

        return Result.success(providers);
    }
}