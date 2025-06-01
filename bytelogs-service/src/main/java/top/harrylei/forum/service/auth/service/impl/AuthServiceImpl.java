package top.harrylei.forum.service.auth.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.user.LoginTypeEnum;
import top.harrylei.forum.api.model.exception.ExceptionUtil;
import top.harrylei.forum.api.model.vo.constants.StatusEnum;
import top.harrylei.forum.service.infra.redis.RedisKeyConstants;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.util.BCryptUtil;
import top.harrylei.forum.service.util.JwtUtil;
import top.harrylei.forum.core.util.PasswordUtil;
import top.harrylei.forum.service.infra.redis.RedisService;
import top.harrylei.forum.service.auth.service.AuthService;
import top.harrylei.forum.service.user.converted.UserInfoConverter;
import top.harrylei.forum.service.user.repository.dao.UserDAO;
import top.harrylei.forum.service.user.repository.dao.UserInfoDAO;
import top.harrylei.forum.service.user.repository.entity.UserDO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(String username, String password) {
        // 参数校验
        ExceptionUtil.requireNonEmpty(username, StatusEnum.PARAM_MISSING, "用户名");
        ExceptionUtil.requireNonEmpty(password, StatusEnum.PARAM_MISSING, "密码");

        // 密码格式校验
        if (!PasswordUtil.isValid(password)) {
            ExceptionUtil.error(StatusEnum.USER_PASSWORD_INVALID);
        }

        // 检查用户名是否已存在
        UserDO user = userDAO.getUserByUserName(username);
        ExceptionUtil.errorIf(user != null, StatusEnum.USER_LOGIN_NAME_REPEAT, username);

        // 创建新用户
        UserDO newUser = new UserDO()
                .setUserName(username)
                .setPassword(BCryptUtil.hash(password))
                .setThirdAccountId("")
                .setLoginType(LoginTypeEnum.USER_PWD.getCode());
        userDAO.saveUser(newUser);

        // 创建用户信息
        UserInfoDO newUserInfo = new UserInfoDO()
                .setUserId(newUser.getId())
                .setUserName(username)
                .setPhoto("");
        userInfoDAO.save(newUserInfo);

        // 记录成功日志
        Long userId = newUser.getId();
        log.info("用户注册成功: userId={}, username={}", userId, username);

        return true;
    }

    @Override
    public String login(String username, String password) {
        // 参数校验
        ExceptionUtil.requireNonEmpty(username, StatusEnum.USER_NAME_OR_PASSWORD_EMPTY);
        ExceptionUtil.requireNonEmpty(password, StatusEnum.USER_NAME_OR_PASSWORD_EMPTY);

        // 查找并验证用户
        UserDO user = userDAO.getUserByUserName(username);
        ExceptionUtil.requireNonNull(user, StatusEnum.USER_NOT_EXISTS, "username=" + username);

        // 校验密码
        if (!BCryptUtil.matches(password, user.getPassword())) {
            log.warn("用户密码错误: username={}", username);
            ExceptionUtil.error(StatusEnum.USER_PWD_ERROR);
        }

        // 获取用户信息
        Long userId = user.getId();
        UserInfoDO userInfo = userInfoDAO.getByUserId(userId);
        ExceptionUtil.requireNonNull(userInfo, StatusEnum.USER_NOT_EXISTS, "userId=" + userId);

        // 更新请求上下文
        ReqInfoContext.getContext().setUserId(userId).setUser(UserInfoConverter.toDTO(userInfo));

        // 生成token
        String token = jwtUtil.generateToken(userId, userInfo.getUserRole());

        // 将token存储到Redis，过期时间与JWT一致
        redisService.setObj(getKey(userId), token, jwtUtil.getExpireSeconds());

        log.info("用户登录成功: userId={}, username={}", userId, username);
        return token;
    }

    @Override
    public void logout(String token) {
        if (StringUtils.isBlank(token)) {
            log.warn("注销失败: token为空");
            return;
        }

        try {
            // 解析token获取用户ID
            Long userId = jwtUtil.parseUserId(token);
            if (userId == null) {
                log.warn("注销失败: 无效的token");
                return;
            }

            // 从Redis中删除token
            boolean result = redisService.del(getKey(userId));
            if (result) {
                log.info("用户 userId={} 注销成功", userId);
            } else {
                log.warn("用户 userId={} 注销失败: Redis中不存在token", userId);
            }
        } catch (Exception e) {
            log.error("注销过程发生异常", e);
        }
    }

    private static @NotNull String getKey(Long userId) {
        return RedisKeyConstants.TOKEN_PREFIX + userId;
    }
}
