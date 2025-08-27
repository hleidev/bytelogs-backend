package top.harrylei.forum.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import top.harrylei.forum.core.context.ReqInfoContext;

import java.io.IOException;

/**
 * 请求上下文过滤器
 * <p>
 * 负责初始化和清理请求上下文，作为过滤器链中最先执行的过滤器，
 * 确保后续过滤器和控制器都能使用上下文。每个请求会创建独立的上下文，
 * 并在请求结束时自动清理，防止内存泄漏。
 * </p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReqInfoContextFilter extends OncePerRequestFilter {

    /**
     * 过滤器核心处理方法
     * <p>
     * 为每个请求创建上下文，并在请求处理完成后清理上下文。
     * 使用 try-finally 结构确保即使发生异常，上下文也会被清理。
     * </p>
     *
     * @param request 当前HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链，用于继续执行后续过滤器
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        try {
            // 初始化请求上下文
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();

            // 设置请求基本信息
            reqInfo.setClientIp(getClientIp(request));
            reqInfo.setHost(request.getServerName());
            reqInfo.setPath(requestUri);
            reqInfo.setReferer(request.getHeader("Referer"));
            reqInfo.setUserAgent(request.getHeader("User-Agent"));

            // 添加到上下文
            ReqInfoContext.setContext(reqInfo);

            if (log.isDebugEnabled()) {
                log.debug("请求上下文已初始化: uri={}, ip={}", requestUri, reqInfo.getClientIp());
            }

            // 继续过滤器链
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // 记录异常但不中断请求处理
            log.error("请求处理异常: uri={}", requestUri, e);
            throw e;
        } finally {
            // 请求结束时清理上下文，即使发生异常也会执行
            ReqInfoContext.clear();
            if (log.isDebugEnabled()) {
                log.debug("请求上下文已清理: uri={}", requestUri);
            }
        }
    }

    /**
     * 获取客户端真实IP地址
     * <p>
     * 尝试从各种代理头中获取客户端IP，处理多级代理的情况。
     * </p>
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }
} 