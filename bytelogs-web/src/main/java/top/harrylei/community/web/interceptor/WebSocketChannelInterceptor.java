package top.harrylei.community.web.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import top.harrylei.community.api.enums.user.UserRoleEnum;
import top.harrylei.community.api.model.user.dto.UserInfoDTO;
import top.harrylei.community.core.common.constans.RedisKeyConstants;
import top.harrylei.community.core.util.JwtUtil;
import top.harrylei.community.core.util.RedisUtil;
import top.harrylei.community.service.user.service.cache.UserCacheService;

import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket消息通道拦截器
 * 复用JWT过滤器逻辑，避免重复认证实现
 *
 * @author harry
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final UserCacheService userCacheService;

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 处理STOMP连接命令的认证
            authenticateStompConnection(accessor);
        }

        return message;
    }

    /**
     * 验证STOMP连接认证
     * 复用JWT过滤器的验证逻辑，避免重复实现
     */
    private void authenticateStompConnection(StompHeaderAccessor accessor) {
        try {
            String token = extractToken(accessor);

            if (StringUtils.isBlank(token)) {
                log.warn("STOMP连接缺少token");
                throw new IllegalArgumentException("WebSocket authentication failed: missing token");
            }

            // 复用JWT过滤器的验证逻辑
            AuthResult authResult = validateTokenAndGetUserInfo(token);
            if (authResult != null) {
                // 设置Spring Security认证对象到STOMP会话
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(authResult.userId, null, authResult.authorities);
                accessor.setUser(auth);

                // 存储到会话属性供其他组件使用
                accessor.getSessionAttributes().put("userId", authResult.userId);
                accessor.getSessionAttributes().put("userInfo", authResult.userInfo);
                accessor.getSessionAttributes().put("isAdmin", authResult.isAdmin);

                log.info("STOMP连接认证成功: userId={}", authResult.userId);
                return;
            }

            log.warn("STOMP连接认证失败");
            throw new IllegalArgumentException("WebSocket authentication failed");

        } catch (Exception e) {
            log.error("STOMP连接认证异常: {}", e.getMessage());
            throw new IllegalArgumentException("WebSocket authentication error", e);
        }
    }

    /**
     * 认证结果内部类
     */
    private record AuthResult(Long userId, UserInfoDTO userInfo, List<SimpleGrantedAuthority> authorities,
                              boolean isAdmin) {
    }

    /**
     * 验证Token并获取用户信息
     * 这里复用JWT过滤器中相同的验证逻辑
     */
    private AuthResult validateTokenAndGetUserInfo(String token) {
        if (StringUtils.isBlank(token) || jwtUtil.isTokenExpired(token)) {
            return null;
        }

        try {
            // 1. 验证token（与JWT过滤器相同逻辑）
            Long userId = jwtUtil.extractUserId(token);
            if (userId == null) {
                return null;
            }

            String redisToken = redisUtil.get(RedisKeyConstants.getUserTokenKey(userId), String.class);
            if (StringUtils.isBlank(redisToken) || !token.equals(redisToken)) {
                return null;
            }

            // 2. 获取用户信息（与JWT过滤器相同逻辑）
            UserRoleEnum role = jwtUtil.extractUserRole(token);
            UserInfoDTO userInfo = userCacheService.getUserInfo(userId);

            if (userInfo != null) {
                boolean isAdmin = UserRoleEnum.ADMIN.equals(role);
                List<SimpleGrantedAuthority> authorities = buildAuthorities(isAdmin);
                return new AuthResult(userId, userInfo, authorities, isAdmin);
            }

        } catch (Exception e) {
            log.error("Token验证异常: {}", e.getMessage(), e);
        }

        return null;
    }

    /**
     * 从多个来源提取JWT token
     */
    private String extractToken(StompHeaderAccessor accessor) {
        // 1. 从Authorization header获取
        List<String> authorization = accessor.getNativeHeader("Authorization");
        if (authorization != null && !authorization.isEmpty()) {
            String authHeader = authorization.getFirst();
            if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }

        // 2. 从自定义header获取
        List<String> tokenHeader = accessor.getNativeHeader("token");
        if (tokenHeader != null && !tokenHeader.isEmpty()) {
            return tokenHeader.getFirst();
        }

        // 3. 从查询参数获取（如果在握手时传递）
        Object sessionAttrs = accessor.getSessionAttributes();
        if (sessionAttrs != null) {
            // 这种情况下token可能在握手时已经验证并存储
            return null;
        }

        return null;
    }


    /**
     * 构建用户权限列表
     * 与JWT过滤器保持一致
     */
    private List<SimpleGrantedAuthority> buildAuthorities(boolean isAdmin) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_NORMAL"));

        if (isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return authorities;
    }
}