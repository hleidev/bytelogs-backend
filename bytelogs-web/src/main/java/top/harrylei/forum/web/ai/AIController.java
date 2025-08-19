package top.harrylei.forum.web.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.harrylei.forum.api.enums.ResultCode;
import top.harrylei.forum.api.enums.ai.AIConversationStatusEnum;
import top.harrylei.forum.api.model.ai.dto.AIConversationDTO;
import top.harrylei.forum.api.model.ai.dto.AIMessageDTO;
import top.harrylei.forum.api.model.ai.req.ChatReq;
import top.harrylei.forum.api.model.ai.req.ConversationsQueryParam;
import top.harrylei.forum.api.model.ai.req.MessagesQueryParam;
import top.harrylei.forum.api.model.ai.vo.AIConversationDetailVO;
import top.harrylei.forum.api.model.ai.vo.AIConversationVO;
import top.harrylei.forum.api.model.ai.vo.AIMessageVO;
import top.harrylei.forum.api.model.ai.vo.AIUsageStatsVO;
import top.harrylei.forum.api.model.base.ResVO;
import top.harrylei.forum.api.model.page.PageVO;
import top.harrylei.forum.core.config.AILimitConfig;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.security.permission.RequiresLogin;
import top.harrylei.forum.core.util.NumUtil;
import top.harrylei.forum.core.util.PageUtils;
import top.harrylei.forum.service.ai.converted.AIConversationStructMapper;
import top.harrylei.forum.service.ai.converted.AIMessageStructMapper;
import top.harrylei.forum.service.ai.repository.entity.AIUsageStatsDO;
import top.harrylei.forum.service.ai.service.AIService;
import top.harrylei.forum.service.ai.service.AIUsageService;

import java.time.LocalDate;

/**
 * AI对话控制器
 *
 * @author harry
 */
@RestController
@RequestMapping("/v1/ai")
@Tag(name = "AI对话模块", description = "AI对话相关接口")
@RequiresLogin
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final AIUsageService aiUsageService;
    private final AILimitConfig aiLimitConfig;
    private final AIConversationStructMapper aiConversationStructMapper;
    private final AIMessageStructMapper aiMessageStructMapper;

    @PostMapping("/chat")
    @Operation(summary = "发起AI对话", description = "发送消息给AI并获取回复")
    public ResVO<AIMessageVO> chat(@Valid @RequestBody ChatReq req) {
        AIMessageDTO aiMessage = aiService.chat(req.getMessage(), req.getConversationId(), req.getModel());
        AIMessageVO messageVO = aiMessageStructMapper.toVO(aiMessage);
        return ResVO.ok(messageVO);
    }

    @GetMapping("/conversations/page")
    @Operation(summary = "获取对话列表", description = "获取当前用户的对话列表（进行中）")
    public ResVO<PageVO<AIConversationVO>> pageQuery(@Valid ConversationsQueryParam queryParam) {
        Long userId = getCurrentUserId();
        PageVO<AIConversationDTO> conversationPage = aiService.pageQueryConversations(userId, queryParam, AIConversationStatusEnum.ACTIVE);
        return ResVO.ok(PageUtils.map(conversationPage, aiConversationStructMapper::toVO));
    }

    @GetMapping("/conversations/archived/page")
    @Operation(summary = "获取归档对话列表", description = "获取当前用户的归档对话列表")
    public ResVO<PageVO<AIConversationVO>> pageQueryArchived(@Valid ConversationsQueryParam queryParam) {
        Long userId = getCurrentUserId();
        PageVO<AIConversationDTO> conversationPage = aiService.pageQueryConversations(userId, queryParam, AIConversationStatusEnum.ARCHIVED);
        return ResVO.ok(PageUtils.map(conversationPage, aiConversationStructMapper::toVO));
    }

    @GetMapping("/conversations/{id}")
    @Operation(summary = "获取对话详情", description = "获取指定对话的详细信息和消息历史")
    public ResVO<AIConversationDetailVO> getConversationDetail(@NotNull(message = "会话ID不能为空") @PathVariable Long id) {

        Long userId = getCurrentUserId();
        AIConversationDTO conversation = aiService.getConversationDetail(id, userId);

        // 获取最新的消息列表（首屏显示）
        MessagesQueryParam queryParam = new MessagesQueryParam();
        queryParam.setPageNum(1);
        queryParam.setPageSize(1);
        PageVO<AIMessageDTO> messagePage = aiService.pageQueryMessages(id, userId, queryParam);

        // 转换DTO为VO
        AIConversationDetailVO pageResult = aiConversationStructMapper.toDetailVO(conversation);
        pageResult.setMessages(PageUtils.map(messagePage, aiMessageStructMapper::toVO).getContent());

        return ResVO.ok(pageResult);
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "获取对话消息", description = "支持基于时间游标的滚动加载")
    public ResVO<PageVO<AIMessageVO>> pageQueryMessages(@NotNull(message = "会话ID不能为空") @PathVariable Long id,
                                                        @Valid MessagesQueryParam queryParam) {
        Long userId = getCurrentUserId();
        PageVO<AIMessageDTO> messagePage = aiService.pageQueryMessages(id, userId, queryParam);

        // 使用PageUtils进行DTO到VO的转换
        return ResVO.ok(PageUtils.map(messagePage, aiMessageStructMapper::toVO));
    }

    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "删除对话", description = "删除指定的对话")
    public ResVO<Void> deleteConversation(@NotNull(message = "对话ID不能为空") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        aiService.deleteConversation(id, userId);

        return ResVO.ok();
    }

    @PutMapping("/conversations/{id}/archive")
    @Operation(summary = "归档对话", description = "归档指定的对话")
    public ResVO<Void> archiveConversation(@NotNull(message = "对话ID不能为空") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        aiService.archiveConversation(id, userId);

        return ResVO.ok();
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
    public ResVO<AIUsageStatsVO> getUsageStats() {
        Long userId = getCurrentUserId();
        LocalDate today = LocalDate.now();

        AIUsageStatsDO todayUsage = aiUsageService.getDailyUsage(userId, today);

        AIUsageStatsVO result = new AIUsageStatsVO();
        result.setDate(today);

        if (todayUsage != null) {
            result.setMessageCount(todayUsage.getMessageCount());
            result.setTokensUsed(todayUsage.getTokensUsed());
            result.setConversationCount(todayUsage.getConversationCount());
        } else {
            result.setMessageCount(0);
            result.setTokensUsed(0);
            result.setConversationCount(0);
        }

        // 设置限制和剩余量
        result.setDailyMessageLimit(aiLimitConfig.getDailyMessageLimit());
        result.setDailyTokenLimit(aiLimitConfig.getDailyTokenLimit());
        result.setRemainingMessages(aiUsageService.getRemainingMessages(userId));

        // 计算剩余Token，避免负数
        int remainingTokens = Math.max(0, aiLimitConfig.getDailyTokenLimit() - result.getTokensUsed());
        result.setRemainingTokens(remainingTokens);

        return ResVO.ok(result);
    }

}