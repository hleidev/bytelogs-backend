package top.harrylei.community.web.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 *
 * @author harry
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单消息代理，处理以"/topic"和"/queue"为前缀的消息
        // /topic - 广播消息（一对多，如系统通知）
        // /queue - 点对点消息（一对一，如个人通知、AI回复）
        registry.enableSimpleBroker("/topic", "/queue");

        // 设置应用程序目的地前缀，客户端发送消息的目的地前缀
        registry.setApplicationDestinationPrefixes("/app");

        // 设置用户目的地前缀，用于点对点消息
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点，客户端连接的WebSocket端点
        registry.addEndpoint("/ws")
                // 配置允许的域名，生产环境应根据实际情况配置
                .setAllowedOriginPatterns("http://localhost:*", "https://localhost:*", "https://*.yourdomain.com")
                // 启用SockJS降级选项，支持不兼容WebSocket的浏览器
                .withSockJS();

        log.info("WebSocket STOMP endpoint registered at /ws");
    }
}