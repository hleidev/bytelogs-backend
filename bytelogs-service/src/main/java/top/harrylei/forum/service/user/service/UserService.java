package top.harrylei.forum.service.user.service;

import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息DTO，不存在则返回null
     */
    BaseUserInfoDTO getUserInfoById(Long userId);

    /**
     * 更新用户信息
     *
     * @param userInfo 需要更新的用户信息DTO
     * @throws RuntimeException 更新失败时抛出异常
     */
    void updateUserInfo(BaseUserInfoDTO userInfo);

    /**
     * 更新用户密码
     * 
     * @param token token
     * @param oldPassword 新密码
     * @param newPassword 旧密码
     */
    void updatePassword(String token, String oldPassword, String newPassword);
}
