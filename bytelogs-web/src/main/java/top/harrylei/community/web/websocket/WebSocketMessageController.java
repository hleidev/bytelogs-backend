package top.harrylei.community.web.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Optional;

/**
 * WebSocket消息控制器
 *
 * @author harry
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final WebSocketSessionManager sessionManager;

    /**
     * 处理心跳消息
     * 客户端发送到 /app/heartbeat
     */
    @MessageMapping("/heartbeat")
    public void handleHeartbeat(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getUserIdFromHeader(headerAccessor);
        if (userId != null) {
            log.debug("Received heartbeat from user: {}", userId);
            // 可以在这里更新用户的最后活跃时间
        }
    }

    /**
     * 处理系统消息订阅确认
     * 客户端发送到 /app/subscribe
     */
    @MessageMapping("/subscribe")
    public void handleSubscribe(@Payload String destination, SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getUserIdFromHeader(headerAccessor);
        if (userId != null) {
            log.info("User {} subscribed to {}", userId, destination);
            sessionManager.sendSystemMessage(userId, "Subscription confirmed: " + destination);
        }
    }

    /**
     * 处理客户端状态报告
     * 客户端发送到 /app/status
     */
    @MessageMapping("/status")
    public void handleStatus(@Payload String status, SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getUserIdFromHeader(headerAccessor);
        if (userId != null) {
            log.debug("Received status from user {}: {}", userId, status);
        }
    }

    /**
     * 从消息头中获取用户ID
     */
    private Long getUserIdFromHeader(SimpMessageHeaderAccessor headerAccessor) {
        try {
            return Optional.ofNullable(headerAccessor.getSessionAttributes())
                    .map(attrs -> (Long) attrs.get("userId"))
                    .orElse(null);
        } catch (Exception e) {
            log.error("Failed to get user ID from message header", e);
            return null;
        }
    }
}