package top.harrylei.forum.service.user.service;

import java.util.List;

import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.enums.user.UserStatusEnum;
import top.harrylei.forum.api.model.vo.auth.UserCreateReq;
import top.harrylei.forum.api.model.vo.page.Page;
import top.harrylei.forum.api.model.vo.page.param.UserQueryParam;
import top.harrylei.forum.api.model.vo.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.api.model.vo.user.dto.UserDetailDTO;

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
    UserInfoDetailDTO getUserInfoById(Long userId);

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
     * @param userId 用户ID
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

    /**
     * 用户列表查询
     *
     * @param queryParam 查询参数
     * @param pageRequest 分页参数
     * @return 用户列表
     */
    List<UserDetailDTO> listUsers(UserQueryParam queryParam, Page pageRequest);

    /**
     * 统计符合条件的用户数量
     *
     * @param queryParam 查询参数
     * @return 用户数量
     */
    long countUsers(UserQueryParam queryParam);

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
     * @param userId 用户ID
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
     * @param role 角色枚举
     */
    void updateUserRole(Long userId, UserRoleEnum role);

    /**
     * 新建用户账号
     *
     * @param req 新建用户的请求参数
     */
    void save(UserCreateReq req);
}
