package top.harrylei.community.web.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import top.harrylei.community.api.enums.websocket.WebSocketMessageType;
import top.harrylei.community.api.model.websocket.message.AiStreamMessage;
import top.harrylei.community.api.model.websocket.message.NotificationMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket会话管理器
 *
 * @author harry
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private final SimpMessagingTemplate messagingTemplate;

    // 在线用户统计
    private final AtomicInteger onlineUsers = new AtomicInteger(0);

    // 用户连接映射（用于统计和管理）
    private final ConcurrentHashMap<Long, String> userSessions = new ConcurrentHashMap<>();

    /**
     * 用户连接时调用
     */
    public void addUser(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            log.warn("Invalid parameters for addUser: userId={}, sessionId={}", userId, sessionId);
            return;
        }
        
        userSessions.put(userId, sessionId);
        int currentCount = onlineUsers.incrementAndGet();
        log.info("User {} connected, session: {}, total online users: {}", userId, sessionId, currentCount);

        // 发送连接成功消息
        sendToUser(userId, WebSocketMessageType.CONNECTION, "Connected successfully");
    }

    /**
     * 用户断开连接时调用
     */
    public void removeUser(Long userId) {
        if (userId == null) {
            log.warn("Cannot remove user: userId is null");
            return;
        }
        
        String sessionId = userSessions.remove(userId);
        if (sessionId != null) {
            int currentCount = onlineUsers.decrementAndGet();
            log.info("User {} disconnected, session: {}, total online users: {}", userId, sessionId, currentCount);
        } else {
            log.debug("User {} was not online when disconnect attempt", userId);
        }
    }

    /**
     * 发送AI流式响应消息
     */
    public void sendAiStream(Long userId, AiStreamMessage message) {
        log.info("发送AI流式消息到用户 {}: conversationId={}, messageId={}, content={}, finished={}", 
                 userId, message.getConversationId(), message.getMessageId(), 
                 message.getContent() != null ? message.getContent().substring(0, Math.min(50, message.getContent().length())) : null, 
                 message.getFinished());
        sendToUser(userId, WebSocketMessageType.AI_STREAM, message);
    }

    /**
     * 发送通知消息
     */
    public void sendNotification(Long userId, NotificationMessage message) {
        sendToUser(userId, WebSocketMessageType.NOTIFICATION, message);
    }

    /**
     * 发送系统消息
     */
    public void sendSystemMessage(Long userId, String message) {
        sendToUser(userId, WebSocketMessageType.SYSTEM, message);
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcastMessage(WebSocketMessageType type, Object message) {
        messagingTemplate.convertAndSend("/topic/broadcast", createMessage(type, message));
        log.debug("Broadcast message sent, type: {}", type);
    }

    /**
     * 发送消息给特定用户（点对点）
     */
    private void sendToUser(Long userId, WebSocketMessageType type, Object data) {
        if (userId == null || type == null) {
            log.warn("Invalid parameters for sendToUser: userId={}, type={}", userId, type);
            return;
        }
        
        try {
            // 使用 Spring STOMP 的用户目的地，会自动路由到对应用户的会话
            String destination = "/v1/queue/messages";
            Object message = createMessage(type, data);

            messagingTemplate.convertAndSendToUser(userId.toString(), destination, message);
            log.debug("Message sent to user {}, type: {}", userId, type);
        } catch (Exception e) {
            log.error("Failed to send message to user {}, type: {}", userId, type, e);
        }
    }

    /**
     * 创建统一格式的消息
     */
    private Object createMessage(WebSocketMessageType type, Object data) {
        return new WebSocketMessageWrapper(type, data, System.currentTimeMillis());
    }

    /**
     * 消息包装器
     */
    private record WebSocketMessageWrapper(WebSocketMessageType type, Object data, Long timestamp) {
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return onlineUsers.get();
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return userSessions.containsKey(userId);
    }
}