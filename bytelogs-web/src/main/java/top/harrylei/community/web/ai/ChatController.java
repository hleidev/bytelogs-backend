package top.harrylei.community.web.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.harrylei.community.api.enums.ai.ChatConversationStatusEnum;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.model.ai.dto.ChatConversationDTO;
import top.harrylei.community.api.model.ai.dto.ChatMessageDTO;
import top.harrylei.community.api.model.ai.dto.ChatUsageStatsDTO;
import top.harrylei.community.api.model.ai.req.ChatReq;
import top.harrylei.community.api.model.ai.req.ConversationsQueryParam;
import top.harrylei.community.api.model.ai.req.MessagesQueryParam;
import top.harrylei.community.api.model.ai.vo.ChatConversationDetailVO;
import top.harrylei.community.api.model.ai.vo.ChatConversationVO;
import top.harrylei.community.api.model.ai.vo.ChatMessageVO;
import top.harrylei.community.api.model.ai.vo.ChatUsageStatsVO;
import top.harrylei.community.api.model.base.Result;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.core.config.AILimitConfig;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.security.permission.RequiresLogin;
import top.harrylei.community.core.util.NumUtil;
import top.harrylei.community.core.util.PageUtils;
import top.harrylei.community.service.ai.converted.ChatConversationStructMapper;
import top.harrylei.community.service.ai.converted.ChatMessageStructMapper;
import top.harrylei.community.service.ai.service.ChatService;
import top.harrylei.community.service.ai.service.ChatUsageService;

import java.time.LocalDate;

/**
 * AI对话控制器
 *
 * @author harry
 */
@RestController
@RequestMapping("/v1/ai/chat")
@Tag(name = "AI对话模块", description = "AI对话相关接口")
@RequiresLogin
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatUsageService chatUsageService;
    private final AILimitConfig aiLimitConfig;
    private final ChatConversationStructMapper chatConversationStructMapper;
    private final ChatMessageStructMapper chatMessageStructMapper;

    @PostMapping
    @Operation(summary = "发起AI对话", description = "发送消息给AI并获取回复")
    public Result<ChatMessageVO> chat(@Valid @RequestBody ChatReq req) {
        ChatMessageDTO aiMessage = chatService.chat(req);
        ChatMessageVO messageVO = chatMessageStructMapper.toVO(aiMessage);
        return Result.success(messageVO);
    }

    @GetMapping("/conversations/page")
    @Operation(summary = "获取对话列表", description = "获取当前用户的对话列表（进行中）")
    public Result<PageVO<ChatConversationVO>> pageQuery(@Valid ConversationsQueryParam queryParam) {
        Long userId = getCurrentUserId();
        PageVO<ChatConversationDTO> conversationPage = chatService.pageQueryConversations(userId, queryParam, ChatConversationStatusEnum.ACTIVE);
        return Result.success(PageUtils.map(conversationPage, chatConversationStructMapper::toVO));
    }

    @GetMapping("/conversations/archived/page")
    @Operation(summary = "获取归档对话列表", description = "获取当前用户的归档对话列表")
    public Result<PageVO<ChatConversationVO>> pageQueryArchived(@Valid ConversationsQueryParam queryParam) {
        Long userId = getCurrentUserId();
        PageVO<ChatConversationDTO> conversationPage = chatService.pageQueryConversations(userId, queryParam, ChatConversationStatusEnum.ARCHIVED);
        return Result.success(PageUtils.map(conversationPage, chatConversationStructMapper::toVO));
    }

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

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "获取对话消息", description = "支持基于时间游标的滚动加载")
    public Result<PageVO<ChatMessageVO>> pageQueryMessages(@NotNull(message = "会话ID不能为空") @PathVariable Long id,
                                                           @Valid MessagesQueryParam queryParam) {
        Long userId = getCurrentUserId();
        PageVO<ChatMessageDTO> messagePage = chatService.pageQueryMessages(id, userId, queryParam);

        // 使用PageUtils进行DTO到VO的转换
        return Result.success(PageUtils.map(messagePage, chatMessageStructMapper::toVO));
    }

    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "删除对话", description = "删除指定的对话")
    public Result<Void> deleteConversation(@NotNull(message = "对话ID不能为空") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        chatService.deleteConversation(id, userId);

        return Result.success();
    }

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


}