package top.harrylei.forum.service.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.enums.user.UserStatusEnum;
import top.harrylei.forum.api.model.vo.auth.UserCreateReq;
import top.harrylei.forum.api.model.vo.user.dto.UserDetailDTO;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.user.service.UserManagementService;
import top.harrylei.forum.service.user.service.UserService;

/**
 * 用户管理服务实现类
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    @Value("${user.default-password}")
    private String defaultPassword;

    private final UserService userService;


    /**
     * 查询用户详细信息
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    @Override
    public UserDetailDTO getUserDetail(Long userId) {
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_MISSING, "用户ID");
        return userService.getUserDetail(userId);
    }

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 新状态
     */
    @Override
    public void updateStatus(Long userId, UserStatusEnum status) {
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_MISSING, "用户ID");
        ExceptionUtil.requireValid(status, ErrorCodeEnum.PARAM_MISSING, "用户状态");

        userService.updateStatus(userId, status);
    }

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     */
    @Override
    public void resetPassword(Long userId) {
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_MISSING, "用户ID");

        userService.resetPassword(userId, defaultPassword);
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    @Override
    public void delete(Long userId) {
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_MISSING, "用户ID");

        userService.updateDeleted(userId, YesOrNoEnum.YES);
    }

    /**
     * 恢复用户
     *
     * @param userId 用户ID
     */
    @Override
    public void restore(Long userId) {
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_MISSING, "用户ID");

        userService.updateDeleted(userId, YesOrNoEnum.NO);
    }

    /**
     * 修改用户角色
     *
     * @param userId 用户ID
     * @param role   角色枚举
     */
    @Override
    public void updateUserRole(Long userId, UserRoleEnum role) {
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_MISSING, "用户ID");
        ExceptionUtil.requireValid(role, ErrorCodeEnum.PARAM_MISSING, "角色");

        userService.updateUserRole(userId, role);
    }

    /**
     * 新建用户账号
     *
     * @param req 新建用户的请求参数
     */
    @Override
    public void save(UserCreateReq req) {
        ExceptionUtil.requireValid(req, ErrorCodeEnum.PARAM_MISSING, "请求参数");

        userService.save(req);
    }
}
