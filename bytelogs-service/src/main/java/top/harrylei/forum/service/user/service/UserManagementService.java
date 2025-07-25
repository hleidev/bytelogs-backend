package top.harrylei.forum.service.user.service;

import top.harrylei.forum.api.model.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.enums.user.UserStatusEnum;
import top.harrylei.forum.api.model.vo.auth.UserCreateReq;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.UserQueryParam;
import top.harrylei.forum.api.model.vo.user.dto.UserDetailDTO;

public interface UserManagementService {


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
     */
    void resetPassword(Long userId);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    void delete(Long userId);

    /**
     * 恢复用户
     *
     * @param userId 用户ID
     */
    void restore(Long userId);

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
