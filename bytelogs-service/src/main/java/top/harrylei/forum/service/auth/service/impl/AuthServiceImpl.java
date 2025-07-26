package top.harrylei.forum.service.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.forum.api.enums.ErrorCodeEnum;
import top.harrylei.forum.api.enums.user.LoginTypeEnum;
import top.harrylei.forum.api.enums.user.UserRoleEnum;
import top.harrylei.forum.api.enums.user.UserStatusEnum;
import top.harrylei.forum.api.model.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.core.util.BCryptUtil;
import top.harrylei.forum.core.util.JwtUtil;
import top.harrylei.forum.core.util.PasswordUtil;
import top.harrylei.forum.core.util.RedisUtil;
import top.harrylei.forum.service.auth.service.AuthService;
import top.harrylei.forum.service.user.repository.dao.UserDAO;
import top.harrylei.forum.service.user.repository.dao.UserInfoDAO;
import top.harrylei.forum.service.user.repository.entity.UserDO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;
import top.harrylei.forum.service.user.service.cache.UserCacheService;

import java.util.Objects;

/**
 * 登录注册服务实现类
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserDAO userDAO;
    private final UserInfoDAO userInfoDAO;
    private final RedisUtil redisUtil;
    private final UserCacheService userCacheService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 密码
     * @param userRole 用户角色
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(String username, String password, UserRoleEnum userRole) {
        // 密码格式校验
        ExceptionUtil.errorIf(!PasswordUtil.isValid(password), ErrorCodeEnum.USER_PASSWORD_INVALID);

        // 检查用户名是否已存在
        UserDO user = userDAO.getUserByUserName(username);
        ExceptionUtil.errorIf(user != null, ErrorCodeEnum.USER_EXISTS, username);

        // 创建新用户
        UserDO newUser = new UserDO()
                .setUserName(username)
                .setPassword(BCryptUtil.hash(password))
                .setThirdAccountId("")
                .setLoginType(LoginTypeEnum.USER_PWD.getCode());
        userDAO.save(newUser);

        // 创建用户信息
        UserInfoDO newUserInfo = new UserInfoDO().setUserId(newUser.getId()).setUserName(username).setAvatar("");

        if (UserRoleEnum.ADMIN.equals(userRole)) {
            // 创建管理员账号需要管理员权限
            if (!ReqInfoContext.getContext().isAdmin()) {
                ExceptionUtil.error(ErrorCodeEnum.FORBID_ERROR_MIXED, "当前用户没有管理员权限");
            }
            newUserInfo.setUserRole(userRole.getCode());
        } else {
            // 创建普通用户
            newUserInfo.setUserRole(UserRoleEnum.NORMAL.getCode());
        }

        userInfoDAO.save(newUserInfo);

        // 简洁、标准化的日志
        log.info("用户注册成功 userId={}", newUser.getId());
    }

    /**
     * 用户登录（普通用户）
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的 Token
     */
    @Override
    public String login(String username, String password) {
        return login(username, password, null);
    }

    /**
     * 用户登录（指定角色）
     *
     * @param username 用户名
     * @param password 密码
     * @param userRole 角色类型（可选）
     * @return 登录成功的 Token
     */
    @Override
    public String login(String username, String password, UserRoleEnum userRole) {
        // 查找用户
        UserDO user = userDAO.getUserByUserName(username);
        ExceptionUtil.requireValid(user, ErrorCodeEnum.USER_NOT_EXISTS, username);

        // 校验账号是否启用
        ExceptionUtil.errorIf(!Objects.equals(user.getStatus(), UserStatusEnum.ENABLED.getCode()),
                              ErrorCodeEnum.USER_DISABLED);

        // 校验密码
        ExceptionUtil.errorIf(!BCryptUtil.matches(password, user.getPassword()),
                              ErrorCodeEnum.USER_USERNAME_OR_PASSWORD_ERROR);

        // 获取用户ID和信息
        Long userId = user.getId();
        UserInfoDetailDTO userInfoDTO = userCacheService.getUserInfo(userId);

        // 校验角色权限
        if (userRole != null && ReqInfoContext.getContext().isAdmin()) {
            ExceptionUtil.errorIf(!Objects.equals(userInfoDTO.getRole(), userRole), ErrorCodeEnum.FORBID_ERROR_MIXED,
                                  "当前用户无管理员权限");
        }

        // 生成token
        String token = jwtUtil.generateToken(userId, userInfoDTO.getRole());

        // 更新上下文
        ReqInfoContext.getContext().setUserId(userId).setUser(userInfoDTO);

        // 缓存token和用户信息
        redisUtil.set(RedisKeyConstants.getUserTokenKey(userId), token, jwtUtil.getExpireSeconds());

        // 安全相关事件保留日志
        log.info("用户登录成功 userId={}", userId);
        return token;
    }

    /**
     * 用户登出
     *
     * @param userId 用户ID
     */
    @Override
    public void logout(Long userId) {
        try {
            boolean result = redisUtil.delete(RedisKeyConstants.getUserTokenKey(userId));
            userCacheService.clearUserInfoCache(userId);

            if (!result) {
                log.warn("退出登录失败 userId={} reason=Redis删除失败", userId);
            } else if (log.isDebugEnabled()) {
                log.debug("退出登录成功 userId={}", userId);
            }
        } catch (Exception e) {
            log.error("退出登录异常 userId={}", userId, e);
        }
    }
}
