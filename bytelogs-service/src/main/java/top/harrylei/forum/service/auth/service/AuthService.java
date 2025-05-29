package top.harrylei.forum.service.auth.service;

/**
 * 用户登录和注册服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 密码
     * @return 注册是否成功
     */
    Boolean register(String username, String password);

    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return JWT token
     */
    String login(String username, String password);

    /**
     * 用户注销
     * 
     * @param token JWT令牌
     */
    void logout(String token);
}
