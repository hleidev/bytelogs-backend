package top.harrylei.forum.service.user.service.impl;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.core.common.RedisKeyConstants;
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
            log.error("更新用户数据失败: userId={}", ReqInfoContext.getContext().getUserId(), e);
            ExceptionUtil.error(StatusEnum.USER_UPDATE_FAILED, "用户信息更新失败，请稍后重试！", e);
        }
    }

    /**
     * 更新用户密码
     *
     * @param token token
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Override
    public void updatePassword(String token, String oldPassword, String newPassword) {
        ExceptionUtil.requireNonEmpty(oldPassword, StatusEnum.PARAM_MISSING, "旧密码为空");
        ExceptionUtil.requireNonEmpty(newPassword, StatusEnum.PARAM_MISSING, "新密码为空");

        Long userId = ReqInfoContext.getContext().getUserId();

        if (Objects.equals(oldPassword, newPassword)) {
            log.warn("新密码与旧密码相同: userId={}", userId);
            ExceptionUtil.error(StatusEnum.USER_UPDATE_FAILED, "新密码与旧密码相同");
        }

        // 校验旧密码
        UserDO user = userDAO.getById(userId);
        ExceptionUtil.requireNonNull(user, StatusEnum.USER_NOT_EXISTS);

        if (!BCryptUtil.matches(oldPassword, user.getPassword())) {
            log.warn("用户密码错误: username={}", user.getUserName());
            ExceptionUtil.error(StatusEnum.USER_PASSWORD_ERROR);
        }

        // 校验新密码安全性
        if (!PasswordUtil.isValid(newPassword)) {
            log.warn("密码必须包含字母、数字，可包含特殊字符，长度为8~20位");
            ExceptionUtil.error(StatusEnum.USER_PASSWORD_INVALID);
        }

        try {
            user.setPassword(BCryptUtil.hash(newPassword));
            userDAO.updateById(user);
            log.info("用户密码更新成功: userId={}", userId);
        } catch (Exception e) {
            log.warn("数据库更新失败: userId={}", userId, e);
            ExceptionUtil.error(StatusEnum.USER_UPDATE_FAILED, "用户密码更新失败，请稍后重试！", e);
        }

        authService.logout(token);
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
        userCacheService.updateUserCache(userInfo);

        try {
            userInfo.setAvatar(avatar);
            userInfoDAO.updateById(userStructMapper.toDO(userInfo));
            log.info("用户头像更新成功: userId={}", userId);
        } catch (Exception e) {
            log.warn("数据库更新失败: userId={}", userId, e);
            ExceptionUtil.error(StatusEnum.USER_UPDATE_FAILED, "用户头像更新失败，请稍候重试！", e);
        }
    }
}
