package top.harrylei.forum.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import top.harrylei.forum.api.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.util.JwtUtil;
import top.harrylei.forum.core.util.RedisUtil;
import top.harrylei.forum.service.user.service.cache.UserCacheService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证过滤器
 *
 * @author harry
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserCacheService userCacheService;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;

    /**
     * 过滤器核心处理方法
     *
     * @param request     当前HTTP请求
     * @param response    HTTP响应
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
                    UserRoleEnum role = jwtUtil.extractUserRole(token);
                    boolean isAdmin = UserRoleEnum.ADMIN.equals(role);

                    // 设置用户认证信息到Spring Security上下文
                    setAuthentication(userId, isAdmin);

                    // 获取完整用户信息
                    UserInfoDetailDTO userInfo = userCacheService.getUserInfo(userId);

                    // 如果获取失败，创建基本用户信息
                    if (userInfo == null) {
                        userInfo = new UserInfoDetailDTO().setUserId(userId).setRole(role);
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
     *
     * @param request 请求
     * @return token或null
     */
    private String getTokenFromRequest(@NotNull HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
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
            Long userId = jwtUtil.extractUserId(token);
            if (userId == null) {
                return null;
            }

            // 从Redis获取存储的token
            String redisToken = redisUtil.get(RedisKeyConstants.getUserTokenKey(userId), String.class);

            // 验证token是否匹配
            if (StringUtils.isBlank(redisToken) || !token.equals(redisToken)) {
                log.debug("Token验证失败: userId={}, token不匹配或已过期", userId);
                return null;
            }

            // 只有在token即将过期时才刷新
            refreshTokenIfNeeded(userId);

            return userId;
        } catch (Exception e) {
            log.error("Token验证异常", e);
            return null;
        }
    }

    /**
     * 设置用户认证信息到Spring Security上下文
     *
     * @param userId  用户ID
     * @param isAdmin 是否为管理员
     */
    private void setAuthentication(Long userId, boolean isAdmin) {
        List<SimpleGrantedAuthority> authorities = buildAuthorities(isAdmin);

        // 创建认证对象
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);

        // 设置认证信息到Spring Security上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 设置用户上下文到ThreadLocal
     *
     * @param userId   用户ID
     * @param userInfo 用户信息
     * @param isAdmin  是否为管理员
     */
    private void setUserContext(Long userId, UserInfoDetailDTO userInfo, boolean isAdmin) {
        List<SimpleGrantedAuthority> authorities = buildAuthorities(isAdmin);

        // 获取当前上下文
        ReqInfoContext.ReqInfo context = ReqInfoContext.getContext();

        // 设置用户信息
        context.setUserId(userId);
        context.setUser(userInfo);
        context.setAuthorities(authorities);
    }

    /**
     * 构建用户权限列表
     *
     * @param isAdmin 是否为管理员
     * @return 权限列表
     */
    private List<SimpleGrantedAuthority> buildAuthorities(boolean isAdmin) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 添加基础角色
        authorities.add(new SimpleGrantedAuthority("ROLE_NORMAL"));

        // 如果是管理员，添加管理员角色
        if (isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return authorities;
    }

    /**
     * 刷新token过期时间
     *
     * @param userId 用户ID
     */
    private void refreshTokenIfNeeded(Long userId) {
        try {
            String tokenKey = RedisKeyConstants.getUserTokenKey(userId);
            Long ttl = redisUtil.ttl(tokenKey);

            if (ttl == null || ttl <= 0) {
                // token已过期或不存在，不需要刷新
                return;
            }

            // 获取默认过期时间（秒）
            long defaultExpireSeconds = jwtUtil.getDefaultExpire().getSeconds();

            // 当剩余时间少于总时间的1/3时才刷新
            if (ttl < defaultExpireSeconds / 3) {
                redisUtil.expire(tokenKey, jwtUtil.getDefaultExpire());
                log.debug("Token过期时间已刷新: userId={}, 剩余时间={}秒", userId, ttl);
            }
        } catch (Exception e) {
            // 刷新失败不影响主流程
            log.warn("Token过期时间刷新失败: userId={}, 错误: {}", userId, e.getMessage());
        }
    }
}