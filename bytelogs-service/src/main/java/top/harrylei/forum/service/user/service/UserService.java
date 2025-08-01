package top.harrylei.forum.service.user.service;

import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.enums.user.UserRoleEnum;
import top.harrylei.forum.api.enums.user.UserStatusEnum;
import top.harrylei.forum.api.model.auth.UserCreateReq;
import top.harrylei.forum.api.model.page.PageVO;
import top.harrylei.forum.api.model.page.param.UserQueryParam;
import top.harrylei.forum.api.model.user.dto.UserDetailDTO;
import top.harrylei.forum.api.model.user.dto.UserInfoDetailDTO;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author harry
 */
public interface UserService {

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息DTO，不存在则返回null
     */
    UserInfoDetailDTO getUserInfoById(Long userId);

    /**
     * 批量获取用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    List<UserInfoDetailDTO> batchQueryUserInfo(List<Long> userIds);

    /**
     * 更新用户信息
     *
     * @param userInfo 需要更新的用户信息DTO
     * @throws RuntimeException 更新失败时抛出异常
     */
    void updateUserInfo(UserInfoDetailDTO userInfo);

    /**
     * 更新用户密码
     *
     * @param userId      用户ID
     * @param oldPassword 新密码
     * @param newPassword 旧密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatar 用户头像
     */
    void updateAvatar(Long userId, String avatar);

    PageVO<UserDetailDTO> pageQuery(UserQueryParam queryParam);

    /**
     * 查询用户详细信息
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    UserDetailDTO getUserDetail(Long userId);

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 新状态
     */
    void updateStatus(Long userId, UserStatusEnum status);

    /**
     * 重置用户密码
     *
     * @param userId   用户ID
     * @param password 新密码
     */
    void resetPassword(Long userId, String password);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @param status 删除状态
     */
    void updateDeleted(Long userId, YesOrNoEnum status);

    /**
     * 修改用户角色
     *
     * @param userId 用户ID
     * @param role   角色枚举
     */
    void updateUserRole(Long userId, UserRoleEnum role);

    /**
     * 新建用户账号
     *
     * @param req 新建用户的请求参数
     */
    void save(UserCreateReq req);
}
