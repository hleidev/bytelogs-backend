package top.harrylei.community.web.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import top.harrylei.community.core.context.ReqInfoContext;

import java.util.Optional;

/**
 * WebSocket事件监听器
 *
 * @author harry
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final WebSocketSessionManager sessionManager;

    /**
     * 监听WebSocket连接事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // 从会话属性中获取用户ID（由ChannelInterceptor设置）
        Long userId = Optional.ofNullable(headerAccessor.getSessionAttributes())
                .map(attrs -> (Long) attrs.get("userId"))
                .orElse(null);

        if (userId == null) {
            log.warn("WebSocket connection without valid user, sessionId: {}", sessionId);
            return;
        }

        // 将用户添加到会话管理器
        sessionManager.addUser(userId, sessionId);

        log.info("WebSocket connected, userId: {}, sessionId: {}", userId, sessionId);
    }

    /**
     * 监听WebSocket断开连接事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        try {
            Optional.ofNullable(headerAccessor.getSessionAttributes())
                    .map(attrs -> (Long) attrs.get("userId"))
                    .ifPresent(sessionManager::removeUser);
        } catch (Exception e) {
            log.error("Error handling WebSocket disconnect event, sessionId: {}", sessionId, e);
        }
    }
}