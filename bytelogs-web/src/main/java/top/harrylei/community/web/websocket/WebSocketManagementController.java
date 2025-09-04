package top.harrylei.community.web.websocket;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.enums.websocket.WebSocketMessageType;
import top.harrylei.community.api.model.base.Result;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.security.permission.RequiresAdmin;
import top.harrylei.community.core.security.permission.RequiresLogin;

/**
 * WebSocket管理控制器
 *
 * @author harry
 */
@Tag(name = "WebSocket管理", description = "WebSocket连接状态和管理接口")
@RestController
@RequestMapping("/v1/websocket")
@RequiredArgsConstructor
public class WebSocketManagementController {

    private final WebSocketSessionManager sessionManager;

    /**
     * 获取在线用户数
     */
    @GetMapping("/online-count")
    @Operation(summary = "获取在线用户数", description = "获取当前WebSocket在线用户总数")
    public Result<Integer> getOnlineUserCount() {
        int count = sessionManager.getOnlineUserCount();
        return Result.success(count);
    }

    /**
     * 检查当前用户是否在线
     */
    @GetMapping("/status")
    @RequiresLogin
    @Operation(summary = "检查连接状态", description = "检查当前用户的WebSocket连接状态")
    public Result<Boolean> checkUserStatus() {
        Long userId = ReqInfoContext.getContext().getUserId();
        boolean isOnline = sessionManager.isUserOnline(userId);
        return Result.success(isOnline);
    }

    /**
     * 向当前用户发送测试消息
     */
    @PostMapping("/test-message")
    @RequiresLogin
    @Operation(summary = "发送测试消息", description = "向当前用户的WebSocket连接发送测试消息")
    public Result<Void> sendTestMessage(@RequestParam String message) {
        if (message == null || message.trim().isEmpty()) {
            ResultCode.INVALID_PARAMETER.throwException("消息内容不能为空");
        }

        Long userId = ReqInfoContext.getContext().getUserId();
        sessionManager.sendSystemMessage(userId, "Test message: " + message.trim());
        return Result.success();
    }

    /**
     * 向指定用户发送系统消息
     */
    @PostMapping("/send-system-message/{userId}")
    @RequiresAdmin
    @Operation(summary = "发送系统消息", description = "向指定用户发送系统消息")
    public Result<Void> sendSystemMessage(@PathVariable Long userId, @RequestParam String message) {
        sessionManager.sendSystemMessage(userId, message);
        return Result.success();
    }

    /**
     * 广播系统消息
     */
    @PostMapping("/broadcast")
    @RequiresAdmin
    @Operation(summary = "广播消息", description = "向所有在线用户广播系统消息")
    public Result<Void> broadcastMessage(@RequestParam String message) {
        sessionManager.broadcastMessage(WebSocketMessageType.SYSTEM, message);
        return Result.success();
    }
}