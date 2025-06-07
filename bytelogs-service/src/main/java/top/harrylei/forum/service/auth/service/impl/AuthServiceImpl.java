package top.harrylei.forum.service.auth.service.impl;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.enums.user.LoginTypeEnum;
import top.harrylei.forum.api.model.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.enums.user.UserStatusEnum;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.core.common.RedisKeyConstants;
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

/**
 * 登录注册服务实现类
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
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String username, String password) {
        // 参数校验
        ExceptionUtil.requireNonEmpty(username, StatusEnum.PARAM_MISSING, "用户名");
        ExceptionUtil.requireNonEmpty(password, StatusEnum.PARAM_MISSING, "密码");

        // 密码格式校验
        ExceptionUtil.errorIf(!PasswordUtil.isValid(password), StatusEnum.USER_PASSWORD_INVALID);

        // 检查用户名是否已存在
        UserDO user = userDAO.getUserByUserName(username);
        ExceptionUtil.errorIf(user != null, StatusEnum.USER_EXISTS, username);

        // 创建新用户
        UserDO newUser = new UserDO().setUserName(username).setPassword(BCryptUtil.hash(password)).setThirdAccountId("")
            .setLoginType(LoginTypeEnum.USER_PWD.getCode());
        userDAO.save(newUser);

        // 创建用户信息
        UserInfoDO newUserInfo = new UserInfoDO().setUserId(newUser.getId()).setUserName(username).setAvatar("");
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
        // 参数校验
        ExceptionUtil.requireNonEmpty(username, StatusEnum.PARAM_MISSING, "用户名");
        ExceptionUtil.requireNonEmpty(password, StatusEnum.PARAM_MISSING, "密码");

        // 查找用户
        UserDO user = userDAO.getUserByUserName(username);
        ExceptionUtil.requireNonNull(user, StatusEnum.USER_NOT_EXISTS, username);

        // 校验账号是否启用
        ExceptionUtil.errorIf(!Objects.equals(user.getStatus(), UserStatusEnum.ENABLE.getCode()),
            StatusEnum.USER_DISABLED);

        // 校验密码
        ExceptionUtil.errorIf(!BCryptUtil.matches(password, user.getPassword()),
            StatusEnum.USER_USERNAME_OR_PASSWORD_ERROR);

        // 获取用户ID和信息
        Long userId = user.getId();
        BaseUserInfoDTO userInfoDTO = userCacheService.getUserInfo(userId);

        // 校验角色权限（如果需要）
        if (userRole != null) {
            ExceptionUtil.errorIf(!Objects.equals(userInfoDTO.getRole(), userRole), StatusEnum.FORBID_ERROR_MIXED,
                "当前用户无管理员权限");
        }

        // 生成token
        String token = jwtUtil.generateToken(userId, userInfoDTO.getRole());

        // 更新上下文
        ReqInfoContext.getContext().setUserId(userId).setUser(userInfoDTO);

        // 缓存token和用户信息
        redisUtil.setObj(RedisKeyConstants.getUserTokenKey(userId), token, jwtUtil.getExpireSeconds());

        // 安全相关事件保留日志
        log.info("用户登录成功 userId={}", userId);
        return token;
    }

    /**
     * 用户登出
     *
     * @param token 请求传入的 Token
     */
    @Override
    public void logout(String token) {
        Long userId = ReqInfoContext.getContext().getUserId();
        if (StringUtils.isBlank(token)) {
            log.warn("退出登录失败 userId={} reason=token为空", userId);
            return;
        }

        try {
            Long userIdFromToken = jwtUtil.parseUserId(token);
            if (userIdFromToken == null) {
                log.warn("退出登录失败 userId={} reason=token解析失败", userId);
                return;
            }

            boolean result = redisUtil.del(RedisKeyConstants.getUserTokenKey(userIdFromToken));
            userCacheService.clearUserCache(userIdFromToken);

            if (!result) {
                log.warn("退出登录失败 userId={} reason=Redis删除失败", userId);
            } else if (log.isDebugEnabled()) {
                log.debug("退出登录成功 userId={}", userId);
            }
        } catch (Exception e) {
            // 简化异常日志，避免冗余
            log.error("退出登录异常 userId={}", userId, e);
        }
    }
}
