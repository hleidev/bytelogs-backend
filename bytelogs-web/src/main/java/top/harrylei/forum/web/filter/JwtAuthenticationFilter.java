package top.harrylei.forum.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.vo.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.util.JwtUtil;
import top.harrylei.forum.core.util.RedisUtil;
import top.harrylei.forum.service.user.service.cache.UserCacheService;

/**
 * JWT认证过滤器
 * <p>
 * 负责解析请求中的JWT令牌，验证其有效性，并设置用户认证信息和上下文。 这是安全框架的核心组件，确保每个受保护的接口都能正确识别用户身份。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserCacheService userCacheService;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;

    /**
     * 过滤器核心处理方法
     * <p>
     * 处理每个HTTP请求，提取JWT令牌并进行认证。 认证成功后，会设置Spring Security上下文和请求上下文，同步获取完整用户信息。
     * </p>
     *
     * @param request 当前HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链，用于继续执行后续过滤器
     */
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
        @NotNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求头中获取JWT令牌
            String token = getTokenFromRequest(request);

            if (StringUtils.isNotBlank(token) && !jwtUtil.isTokenExpired(token)) {
                // 解析JWT令牌获取用户ID
                Long userId = checkRedisToken(token);

                if (userId != null) {
                    // 获取用户角色
                    String role = jwtUtil.parseRole(token);
                    boolean isAdmin = "ADMIN".equals(role);

                    // 设置用户认证信息到Spring Security上下文
                    setAuthentication(userId, isAdmin);

                    // 获取完整用户信息
                    UserInfoDetailDTO userInfo = userCacheService.getUserInfo(userId);

                    // 如果获取失败，创建基本用户信息
                    if (userInfo == null) {
                        userInfo = new UserInfoDetailDTO().setUserId(userId).setRole(UserRoleEnum.fromName(role));
                    }

                    // 设置用户信息到上下文
                    setUserContext(userId, userInfo, isAdmin);

                    log.debug("用户认证成功: userId={}, role={}", userId, role);
                }
            }
        } catch (Exception e) {
            log.warn("JWT认证过程发生异常: {}", e.getMessage(), e);
            // 不抛出异常，继续执行过滤器链，保证请求能够正常处理
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取token
     * @param request 请求
     * @return token或null
     */
    private String getTokenFromRequest(@NotNull HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        final String BEARER_PREFIX = "Bearer ";
        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        log.debug("Authorization header 无效或格式不正确: {}", authHeader);
        return null;
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT令牌
     * @return 如果有效返回用户ID，否则返回null
     */
    private Long checkRedisToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }

        try {
            // 解析JWT获取用户ID
            Long userId = jwtUtil.parseUserId(token);
            if (userId == null) {
                return null;
            }

            // 从Redis获取存储的token
            String redisToken = redisUtil.getObj(RedisKeyConstants.getUserTokenKey(userId), String.class);

            // 验证token是否匹配
            if (StringUtils.isBlank(redisToken) || !token.equals(redisToken)) {
                log.debug("Token验证失败: userId={}, token不匹配或已过期", userId);
                return null;
            }

            // 刷新token过期时间
            redisUtil.expire(RedisKeyConstants.getUserTokenKey(userId), jwtUtil.getExpireSeconds());

            return userId;
        } catch (Exception e) {
            log.error("Token验证异常", e);
            return null;
        }
    }

    /**
     * 设置用户认证信息到Spring Security上下文
     *
     * @param userId 用户ID
     * @param isAdmin 是否为管理员
     */
    private void setAuthentication(Long userId, boolean isAdmin) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 添加基础角色
        authorities.add(new SimpleGrantedAuthority("ROLE_NORMAL"));

        // 如果是管理员，添加管理员角色
        if (isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        // 创建认证对象
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userId, null, authorities);

        // 设置认证信息到Spring Security上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 设置用户上下文到ThreadLocal
     *
     * @param userId 用户ID
     * @param userInfo 用户信息
     * @param isAdmin 是否为管理员
     */
    private void setUserContext(Long userId, UserInfoDetailDTO userInfo, boolean isAdmin) {
        // 构建用户上下文信息
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_NORMAL"));

        if (isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        // 获取当前上下文（已在ReqInfoContext中确保不为null）
        ReqInfoContext.ReqInfo context = ReqInfoContext.getContext();

        // 设置用户信息
        context.setUserId(userId);
        context.setUser(userInfo);
        context.setAuthorities(authorities);

        // 重新设置上下文（为了保持链式调用的兼容性）
        ReqInfoContext.setContext(context);
    }
}