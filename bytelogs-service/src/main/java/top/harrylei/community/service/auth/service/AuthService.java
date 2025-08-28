package top.harrylei.community.service.auth.service;

import top.harrylei.community.api.enums.user.UserRoleEnum;

/**
 * 用户登录和注册服务接口
 *
 * @author harry
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 密码
     * @param userRole 用户角色
     */
    void register(String username, String password, UserRoleEnum userRole);

    /**
     * 用户登录
     *
     * @param username  用户名
     * @param password  密码
     * @param keepLogin 是否保持登录状态
     * @return JWT token
     */
    String login(String username, String password, boolean keepLogin);

    /**
     * 用户登录（带角色验证）
     *
     * @param username  用户名
     * @param password  密码
     * @param keepLogin 是否保持登录状态
     * @param userRole  用户角色
     * @return JWT token
     */
    String login(String username, String password, boolean keepLogin, UserRoleEnum userRole);

    /**
     * 退出登录
     *
     * @param userId JWT令牌
     */
    void logout(Long userId);
}
