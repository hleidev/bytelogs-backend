package top.harrylei.forum.web.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.core.common.RedisKeyConstants;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.util.JwtUtil;
import top.harrylei.forum.core.util.RedisUtil;
import top.harrylei.forum.service.user.service.UserService;

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

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final UserService userService;

    /**
     * 过滤器核心处理方法
     * <p>
     * 处理每个HTTP请求，提取JWT令牌并进行认证。 认证成功后，会设置Spring Security上下文和请求上下文。
     * </p>
     *
     * @param request 当前HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链，用于继续执行后续过滤器
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        try {
            // 从请求头中获取JWT令牌
            String token = parseToken(request);

            if (StringUtils.isNotBlank(token) && !jwtUtil.isTokenExpired(token)) {
                // 解析JWT令牌获取用户ID
                Long userId = checkRedisToken(token);

                if (userId != null) {
                    // 获取用户角色
                    String role = jwtUtil.parseRole(token);
                    boolean isAdmin = "ADMIN".equals(role);

                    // 设置用户认证信息到Spring Security上下文
                    setAuthentication(userId, isAdmin);

                    // 创建简单用户上下文（基本信息）
                    BaseUserInfoDTO basicUserInfo = new BaseUserInfoDTO().setUserId(userId).setRole(role);

                    // 设置基本用户信息到上下文
                    setUserContext(userId, basicUserInfo, isAdmin);

                    // 异步获取用户详细信息并设置上下文（需要查询数据库）
                    userService.getUserInfoAsync(userId, userInfo -> {
                        if (userInfo != null) {
                            try {
                                setUserContext(userId, userInfo, isAdmin);
                            } catch (Exception e) {
                                log.error("设置详细用户上下文异常: userId={}", userId, e);
                            }
                        }
                    });

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
            String redisToken = redisUtil.getObj(RedisKeyConstants.BYTE_LOGS_PREFIX + userId, String.class);

            // 验证token是否匹配
            if (StringUtils.isBlank(redisToken) || !token.equals(redisToken)) {
                log.debug("Token验证失败: userId={}, token不匹配或已过期", userId);
                return null;
            }

            // 刷新token过期时间
            redisUtil.expire(RedisKeyConstants.BYTE_LOGS_PREFIX + userId, jwtUtil.getExpireSeconds());

            return userId;
        } catch (Exception e) {
            log.error("Token验证异常", e);
            return null;
        }
    }

    /**
     * 从请求头中提取JWT令牌
     * <p>
     * 从Authorization头中提取Bearer令牌，格式为"Bearer xxxxx"
     * </p>
     *
     * @param request HTTP请求
     * @return JWT令牌，如果不存在则返回null
     */
    private String parseToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    /**
     * 设置用户认证信息到Spring Security上下文
     * <p>
     * 创建认证对象并设置到Spring Security上下文中，使后续的安全检查能够识别用户身份
     * </p>
     *
     * @param userId 用户ID
     * @param isAdmin 是否为管理员
     */
    private void setAuthentication(Long userId, boolean isAdmin) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 添加基础角色
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

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
     * 设置用户上下文到TransmittableThreadLocal
     * <p>
     * 将用户信息设置到请求上下文中，使业务代码能够访问用户信息。 使用TransmittableThreadLocal确保异步线程也能获取到用户上下文。
     * </p>
     *
     * @param userId 用户ID
     * @param userInfo 用户信息对象
     * @param isAdmin 是否为管理员
     */
    private void setUserContext(Long userId, BaseUserInfoDTO userInfo, boolean isAdmin) {
        // 构建用户上下文信息
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

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