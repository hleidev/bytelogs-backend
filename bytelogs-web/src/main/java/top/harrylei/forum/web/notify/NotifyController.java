package top.harrylei.forum.web.notify;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.notify.req.NotifyMsgQueryParam;
import top.harrylei.forum.api.model.vo.notify.vo.NotifyMsgVO;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.security.permission.RequiresLogin;
import top.harrylei.forum.service.notify.service.NotifyMsgService;

/**
 * 通知消息控制器
 *
 * @author harry
 */
@Tag(name = "通知消息模块", description = "提供通知列表查询等接口")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notify")
@Validated
@RequiresLogin
public class NotifyController {

    private final NotifyMsgService notifyMsgService;

    /**
     * 获取当前用户的通知列表
     *
     * @param param 查询参数
     * @return 通知列表
     */
    @Operation(summary = "获取通知列表", description = "分页查询当前用户的通知消息")
    @GetMapping("/list")
    public ResVO<PageVO<NotifyMsgVO>> getNotificationList(@Valid NotifyMsgQueryParam param) {
        Long userId = ReqInfoContext.getContext().getUserId();
        PageVO<NotifyMsgVO> notifications = notifyMsgService.getMyNotifications(userId, param);
        return ResVO.ok(notifications);
    }

    /**
     * 标记指定通知为已读
     *
     * @param msgId 通知消息ID
     * @return 操作结果
     */
    @Operation(summary = "标记通知为已读", description = "将指定的通知消息标记为已读状态")
    @PostMapping("/read/{msgId}")
    public ResVO<Void> markAsRead(@PathVariable Long msgId) {
        Long userId = ReqInfoContext.getContext().getUserId();
        notifyMsgService.markAsRead(msgId, userId);
        return ResVO.ok();
    }

    /**
     * 标记所有通知为已读
     *
     * @return 操作结果
     */
    @Operation(summary = "标记所有通知为已读", description = "将当前用户的所有通知消息标记为已读状态")
    @PostMapping("/read/all")
    public ResVO<Void> markAllAsRead() {
        Long userId = ReqInfoContext.getContext().getUserId();
        notifyMsgService.markAllAsRead(userId);
        return ResVO.ok();
    }

    /**
     * 获取未读通知数量
     *
     * @return 未读通知数量
     */
    @Operation(summary = "获取未读通知数量", description = "获取当前用户的未读通知消息数量")
    @GetMapping("/unread/count")
    public ResVO<Long> getUnreadCount() {
        Long userId = ReqInfoContext.getContext().getUserId();
        Long count = notifyMsgService.getUnreadCount(userId);
        return ResVO.ok(count);
    }
}