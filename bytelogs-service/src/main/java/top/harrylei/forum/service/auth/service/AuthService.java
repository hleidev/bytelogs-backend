package top.harrylei.forum.service.auth.service;

import top.harrylei.forum.api.enums.user.UserRoleEnum;

/**
 * 用户登录和注册服务接口
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
     * @param username 用户名
     * @param password 密码
     * @return JWT token
     */
    String login(String username, String password);

    /**
     * 带用户角色的登录
     *
     * @param username 用户名
     * @param password 密码
     * @param userRole 用户角色
     * @return JWT token
     */
    String login(String username, String password, UserRoleEnum userRole);

    /**
     * 退出登录
     * 
     * @param userId JWT令牌
     */
    void logout(Long userId);
}
