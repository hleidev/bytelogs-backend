package top.harrylei.forum.service.user.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.enums.user.UserStatusEnum;
import top.harrylei.forum.api.model.vo.page.PageReq;
import top.harrylei.forum.api.model.vo.page.param.UserQueryParam;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.api.model.vo.user.dto.UserDetailDTO;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.core.util.BCryptUtil;
import top.harrylei.forum.core.util.PasswordUtil;
import top.harrylei.forum.core.util.RedisUtil;
import top.harrylei.forum.service.auth.service.AuthService;
import top.harrylei.forum.service.user.converted.UserStructMapper;
import top.harrylei.forum.service.user.repository.dao.UserDAO;
import top.harrylei.forum.service.user.repository.dao.UserInfoDAO;
import top.harrylei.forum.service.user.repository.entity.UserDO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;
import top.harrylei.forum.service.user.service.UserService;
import top.harrylei.forum.service.user.service.cache.UserCacheService;

/**
 * 用户服务实现类
 * <p>
 * 提供用户信息查询和更新功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserInfoDAO userInfoDAO;
    private final UserStructMapper userStructMapper;
    private final UserDAO userDAO;
    private final RedisUtil redisUtil;
    private final AuthService authService;
    private final UserCacheService userCacheService;

    /**
     * 获取用户信息，支持缓存优先
     *
     * @param userId 用户 ID
     * @return 用户信息 DTO
     */
    public BaseUserInfoDTO getUserInfoById(Long userId) {
        ExceptionUtil.errorIf(userId == null, StatusEnum.PARAM_MISSING, "用户ID");

        return userCacheService.getUserInfo(userId);
    }

    /**
     * 更新用户信息
     *
     * @param userInfoDTO 需要更新的用户信息DTO
     * @throws RuntimeException 更新失败时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(BaseUserInfoDTO userInfoDTO) {
        ExceptionUtil.requireNonNull(userInfoDTO, StatusEnum.PARAM_MISSING, "用户信息为空");

        try {
            // 转换为数据库实体并更新用户个人信息
            UserInfoDO userInfo = userStructMapper.toDO(userInfoDTO);
            userInfoDAO.updateById(userInfo);

            // 更新用户账户信息
            UserDO userDO = new UserDO();
            userDO.setId(userInfo.getUserId());
            userDO.setUserName(userInfo.getUserName());
            userDAO.updateById(userDO);

            redisUtil.del(RedisKeyConstants.getUserInfoKey(userInfo.getUserId()));

            log.info("用户信息更新成功: userId={}", userInfoDTO.getUserId());
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.USER_UPDATE_FAILED, "用户信息更新失败，请稍后重试！", e);
        }
    }

    /**
     * 更新用户密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        ExceptionUtil.requireNonNull(userId, StatusEnum.PARAM_MISSING, "用户ID为空");
        ExceptionUtil.requireNonEmpty(oldPassword, StatusEnum.PARAM_MISSING, "旧密码为空");
        ExceptionUtil.requireNonEmpty(newPassword, StatusEnum.PARAM_MISSING, "新密码为空");

        if (Objects.equals(oldPassword, newPassword)) {
            ExceptionUtil.error(StatusEnum.USER_UPDATE_FAILED, "新密码与旧密码相同");
        }

        // 校验旧密码
        UserDO user = userDAO.getUserById(userId);
        ExceptionUtil.requireNonNull(user, StatusEnum.USER_NOT_EXISTS, "userId=" + userId);

        if (!BCryptUtil.matches(oldPassword, user.getPassword())) {
            ExceptionUtil.error(StatusEnum.USER_PASSWORD_ERROR);
        }

        resetPassword(userId, newPassword);

        authService.logout(userId);
    }

    /**
     * 更新用户头像
     *
     * @param avatar 用户头像
     */
    @Override
    public void updateAvatar(String avatar) {
        ExceptionUtil.requireNonEmpty(avatar, StatusEnum.PARAM_MISSING, "用户头像为空");

        Long userId = ReqInfoContext.getContext().getUserId();
        BaseUserInfoDTO userInfo = ReqInfoContext.getContext().getUser();
        ExceptionUtil.requireNonNull(userInfo, StatusEnum.USER_INFO_NOT_EXISTS);

        redisUtil.del(RedisKeyConstants.getUserInfoKey(userInfo.getUserId()));
        userCacheService.updateUserInfoCache(userInfo);

        try {
            userInfo.setAvatar(avatar);
            userInfoDAO.updateById(userStructMapper.toDO(userInfo));
            log.info("用户头像更新成功: userId={}", userId);
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.USER_UPDATE_FAILED, "用户头像更新失败，请稍候重试！", e);
        }
    }

    /**
     * 用户列表查询
     *
     * @param queryParam 查询参数
     * @param pageRequest 分页参数
     * @return 用户列表
     */
    @Override
    public List<UserDetailDTO> listUsers(UserQueryParam queryParam, PageReq pageRequest) {
        ExceptionUtil.requireNonNull(queryParam, StatusEnum.PARAM_MISSING, "请求参数");
        ExceptionUtil.requireNonNull(pageRequest, StatusEnum.PARAM_MISSING, "分页参数");

        try {
            return userDAO.listUsers(queryParam, pageRequest.getLimitSql());
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.SYSTEM_ERROR, "查询用户列表异常", e);
            return List.of();
        }
    }

    /**
     * 统计符合条件的用户数量
     *
     * @param queryParam 查询参数
     * @return 用户数量
     */
    @Override
    public long countUsers(UserQueryParam queryParam) {
        ExceptionUtil.requireNonNull(queryParam, StatusEnum.PARAM_MISSING, "请求参数");

        try {
            return userDAO.countUsers(queryParam);
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.SYSTEM_ERROR, "统计用户数量失败", e);
            return 0;
        }
    }

    /**
     * 查询用户详细信息
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    @Override
    public UserDetailDTO getUserDetail(Long userId) {
        ExceptionUtil.requireNonNull(userId, StatusEnum.PARAM_MISSING, "用户ID");

        try {
            return userDAO.getUserDetail(userId);
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.SYSTEM_ERROR, "查询用户详细信息异常", e);
            return null;
        }
    }

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 新状态
     */
    @Override
    public void updateStatus(Long userId, UserStatusEnum status) {
        ExceptionUtil.requireNonNull(userId, StatusEnum.PARAM_MISSING, "用户ID");
        ExceptionUtil.requireNonNull(status, StatusEnum.PARAM_MISSING, "用户状态");

        UserDO user = userDAO.getById(userId);
        ExceptionUtil.requireNonNull(user, StatusEnum.USER_NOT_EXISTS, "userId=" + userId);

        try {
            user.setStatus(status.getCode());
            userDAO.updateById(user);
            log.info("修改用户状态成功: userId={}", userId);
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.USER_UPDATE_FAILED, "用户状态更新失败，请稍后重试", e);
        }
    }

    /**
     * 重置密码
     *
     * @param userId 用户ID
     * @param password 新密码
     */
    @Override
    public void resetPassword(Long userId, String password) {
        ExceptionUtil.requireNonNull(userId, StatusEnum.PARAM_MISSING, "用户ID");
        // 校验新密码安全性
        if (!PasswordUtil.isValid(password)) {
            ExceptionUtil.error(StatusEnum.USER_PASSWORD_INVALID);
        }

        UserDO user = userDAO.getUserById(userId);
        ExceptionUtil.requireNonNull(user, StatusEnum.USER_NOT_EXISTS, "userId=" + userId);

        try {
            user.setPassword(BCryptUtil.hash(password));
            userDAO.updateById(user);
            log.info("用户密码更新成功: userId={}", userId);
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.USER_UPDATE_FAILED, "用户密码更新失败，请稍后重试！", e);
        }
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long userId) {
        ExceptionUtil.requireNonNull(userId, StatusEnum.PARAM_MISSING, "用户ID");

        UserDO user = userDAO.getUserById(userId);
        ExceptionUtil.requireNonNull(user, StatusEnum.USER_NOT_EXISTS, "userId=" + userId);

        UserInfoDO userInfo = userInfoDAO.getUserInfoById(userId);
        ExceptionUtil.requireNonNull(userInfo, StatusEnum.USER_INFO_NOT_EXISTS, "userId=" + userId);

        Long operatorId = ReqInfoContext.getContext().getUserId();

        try {
            user.setDeleted(YesOrNoEnum.YES.getCode());
            userDAO.updateById(user);
            userInfo.setDeleted(YesOrNoEnum.YES.getCode());
            userInfoDAO.updateById(userInfo);
            log.info("删除用户成功: userId={}, operatorId={}", userId, operatorId);
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.USER_DELETE_FAILED, e);
        }
    }

    /**
     * 恢复用户
     *
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void restore(Long userId) {
        ExceptionUtil.requireNonNull(userId, StatusEnum.PARAM_MISSING, "用户ID为空");

        UserDO user = userDAO.getDeletedUserById(userId);
        ExceptionUtil.requireNonNull(user, StatusEnum.USER_NOT_EXISTS, "userId=" + userId);

        UserInfoDO userInfo = userInfoDAO.getDeletedUserInfoById(userId);
        ExceptionUtil.requireNonNull(userInfo, StatusEnum.USER_INFO_NOT_EXISTS, "userId=" + userId);

        Long operatorId = ReqInfoContext.getContext().getUserId();

        try {
            user.setDeleted(YesOrNoEnum.NO.getCode());
            userDAO.updateById(user);
            userInfo.setDeleted(YesOrNoEnum.NO.getCode());
            userInfoDAO.updateById(userInfo);
            log.info("恢复用户成功: userId={}, operatorId={}", userId, operatorId);
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.USER_RESTORE_FAILED, e);
        }
    }

    /**
     * 修改用户角色
     *
     * @param userId 用户ID
     * @param role 角色枚举
     */
    @Override
    public void updateUserRole(Long userId, UserRoleEnum role) {
        ExceptionUtil.requireNonNull(userId, StatusEnum.PARAM_MISSING, "用户ID");
        ExceptionUtil.requireNonNull(role, StatusEnum.PARAM_MISSING, "角色");

        UserInfoDO userInfo = userInfoDAO.getUserInfoById(userId);
        ExceptionUtil.requireNonNull(userInfo, StatusEnum.USER_INFO_NOT_EXISTS, "userId=" + userId);

        Long operatorId = ReqInfoContext.getContext().getUserId();

        // 判断新旧角色是否一致，避免无效写入
        if (Objects.equals(userInfo.getUserRole(), role.getCode())) {
            log.info("用户角色未变更，无需更新: userId={}, operatorId={}", userId, operatorId);
            return;
        }

        try {
            userInfo.setUserRole(role.getCode());
            userInfoDAO.updateById(userInfo);
            log.info("更新用户角色成功: userId={}, operatorId={}", userId, operatorId);
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.USER_UPDATE_FAILED, "更新用户角色失败 userId=" + userId, e);
        }
    }
}
