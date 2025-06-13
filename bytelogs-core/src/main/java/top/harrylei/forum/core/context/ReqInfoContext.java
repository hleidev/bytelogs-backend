package top.harrylei.forum.core.context;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.alibaba.ttl.TransmittableThreadLocal;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.vo.user.dto.UserInfoDetailDTO;

/**
 * 请求上下文管理类
 * 使用 TransmittableThreadLocal 存储请求上下文信息，支持异步线程传递
 */
@Slf4j
public class ReqInfoContext {

    public static final String ADMIN = "ROLE_ADMIN";
    private static final TransmittableThreadLocal<ReqInfo> content = new TransmittableThreadLocal<>();

    /**
     * 设置当前线程的用户上下文
     * 
     * @param reqInfo 请求信息对象
     */
    public static void setContext(ReqInfo reqInfo) {
        content.set(reqInfo);
    }

    /**
     * 获取当前线程的用户上下文
     * 如果上下文不存在，会自动创建一个新的上下文对象
     * 
     * @return 请求信息对象，不会为 null
     */
    public static ReqInfo getContext() {
        ReqInfo reqInfo = content.get();
        if (reqInfo == null) {
            log.debug("尝试获取未初始化的请求上下文，自动创建空上下文");
            reqInfo = new ReqInfo();
            content.set(reqInfo);
        }
        return reqInfo;
    }

    /**
     * 清除当前线程的用户上下文
     * 应在请求处理完成后调用，避免内存泄漏
     */
    public static void clear() {
        content.remove();
    }

    /**
     * 请求信息对象，包含用户身份和请求相关信息
     */
    @Data
    @Accessors(chain = true)
    public static class ReqInfo {
        /**
         * 用户ID，已登录用户不为空
         */
        private Long userId;

        /**
         * 用户详细信息
         */
        private UserInfoDetailDTO user;

        /**
         * 用户角色权限列表
         */
        private List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        /**
         * 客户端IP地址
         */
        private String clientIp;

        /**
         * 请求域名
         */
        private String host;

        /**
         * 请求路径
         */
        private String path;

        /**
         * 请求来源
         */
        private String referer;

        /**
         * 用户代理信息
         */
        private String userAgent;

        /**
         * 判断当前用户是否为管理员
         * 
         * @return true 如果用户具有管理员权限，否则 false
         */
        public boolean isAdmin() {
            return authorities != null && authorities.stream()
                    .anyMatch(authority -> ADMIN.equals(authority.getAuthority()));
        }
        
        /**
         * 判断当前用户是否已登录
         * 
         * @return true 如果用户已登录，否则 false
         */
        public boolean isLoggedIn() {
            return userId != null && userId > 0;
        }
    }
}
