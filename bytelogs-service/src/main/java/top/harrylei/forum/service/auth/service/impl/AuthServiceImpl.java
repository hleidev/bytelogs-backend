package top.harrylei.forum.service.auth.service.impl;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.enums.user.LoginTypeEnum;
import top.harrylei.forum.api.model.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.enums.user.UserStatusEnum;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.core.util.BCryptUtil;
import top.harrylei.forum.core.util.PasswordUtil;
import top.harrylei.forum.service.auth.service.AuthService;
import top.harrylei.forum.service.infra.redis.RedisKeyConstants;
import top.harrylei.forum.service.infra.redis.RedisService;
import top.harrylei.forum.service.user.converted.UserInfoStructMapper;
import top.harrylei.forum.service.user.repository.dao.UserDAO;
import top.harrylei.forum.service.user.repository.dao.UserInfoDAO;
import top.harrylei.forum.service.user.repository.entity.UserDO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;
import top.harrylei.forum.service.util.JwtUtil;

/**
 * 登录和注册服务实现类
 * <p>
 * 提供用户注册、登录和注销功能，处理身份验证和令牌管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserDAO userDAO;
    private final UserInfoDAO userInfoDAO;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final UserInfoStructMapper userInfoStructMapper;

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

    @Override
    public String login(String username, String password) {
        return login(username, password, null);
    }

    @Override
    public String login(String username, String password, UserRoleEnum userRole) {
        // 参数校验
        ExceptionUtil.requireNonEmpty(username, StatusEnum.PARAM_MISSING, "用户名");
        ExceptionUtil.requireNonEmpty(password, StatusEnum.PARAM_MISSING, "密码");

        // 查找并验证用户
        UserDO user = userDAO.getUserByUserName(username);
        ExceptionUtil.requireNonNull(user, StatusEnum.USER_NOT_EXISTS, username);

        // 校验账号是否启用
        ExceptionUtil.errorIf(!Objects.equals(user.getStatus(), UserStatusEnum.ENABLE.getCode()),
            StatusEnum.USER_DISABLED);

        // 校验密码
        ExceptionUtil.errorIf(!BCryptUtil.matches(password, user.getPassword()),
            StatusEnum.USER_USERNAME_OR_PASSWORD_ERROR);

        // 获取用户信息
        Long userId = user.getId();
        UserInfoDO userInfo = userInfoDAO.getByUserId(userId);
        ExceptionUtil.requireNonNull(userInfo, StatusEnum.USER_INFO_NOT_EXISTS, user.getUserName());

        if (userRole != null) {
            ExceptionUtil.errorIf(!Objects.equals(userInfo.getUserRole(), userRole.getCode()),
                StatusEnum.FORBID_ERROR_MIXED, "当前用户无管理员权限");
        }

        // 更新请求上下文
        ReqInfoContext.getContext().setUserId(userId).setUser(userInfoStructMapper.toDTO(userInfo));

        // 生成token
        String token = jwtUtil.generateToken(userId, userInfo.getUserRole());

        // 将token存储到Redis，过期时间与JWT一致
        redisService.setObj(getKey(userId), token, jwtUtil.getExpireSeconds());

        // 安全相关事件保留日志
        log.info("用户登录成功 userId={}", userId);
        return token;
    }

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

            boolean result = redisService.del(getKey(userIdFromToken));
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

    private static @NotNull String getKey(Long userId) {
        return RedisKeyConstants.getUserTokenKey(userId);
    }
}
