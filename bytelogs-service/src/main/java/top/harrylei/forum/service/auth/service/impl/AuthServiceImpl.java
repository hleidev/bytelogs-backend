package top.harrylei.forum.service.auth.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.user.LoginTypeEnum;
import top.harrylei.forum.api.model.exception.ExceptionUtil;
import top.harrylei.forum.api.model.vo.constants.StatusEnum;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.security.JwtUtil;
import top.harrylei.forum.core.util.BCryptUtil;
import top.harrylei.forum.core.util.PasswordUtil;
import top.harrylei.forum.service.auth.service.AuthService;
import top.harrylei.forum.service.user.converted.UserInfoConverter;
import top.harrylei.forum.service.user.repository.dao.UserDAO;
import top.harrylei.forum.service.user.repository.entity.UserDO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;

/**
 * 登录和注册服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserDAO userDAO;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(String username, String password) {
        // 参数校验
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "用户名或密码不能为空");
        }

        // 密码格式校验
        if (!PasswordUtil.isValid(password)) {
            throw ExceptionUtil.of(StatusEnum.USER_PASSWORD_INVALID);
        }

        // 检查用户名是否已存在
        UserDO user = userDAO.getUserByUserName(username);
        if (user != null) {
            throw ExceptionUtil.of(StatusEnum.USER_LOGIN_NAME_REPEAT, username);
        }

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
        userDAO.save(newUserInfo);

        // 更新上下文并返回token
        Long userId = newUser.getId();
        log.info("用户注册成功: userId={}，username={}", userId, username);

        return true;
    }

    @Override
    public String login(String username, String password) {
        // 参数校验
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw ExceptionUtil.of(StatusEnum.USER_NAME_OR_PASSWORD_EMPTY);
        }

        // 查找用户
        UserDO user = userDAO.getUserByUserName(username);
        if (user == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "username=" + username);
        }

        // 校验密码
        if (!BCryptUtil.matches(password, user.getPassword())) {
            log.warn("用户密码错误: {}", username);
            throw ExceptionUtil.of(StatusEnum.USER_PWD_ERROR);
        }

        // 更新上下文并返回token
        Long userId = user.getId();
        UserInfoDO userInfo = userDAO.getById(user.getId());

        ReqInfoContext.getContext().setUserId(userId).setUser(UserInfoConverter.toDTO(userInfo));
        log.info("用户登录成功: {}", username);

        // TODO 将 token 保存到 Redis 中

        return jwtUtil.generateToken(userId, userInfo.getUserRole());
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

            // TODO 后期删除 Redis 中的 Token
            log.info("用户[{}]注销成功", userId);
        } catch (Exception e) {
            log.error("注销过程发生异常", e);
        }
    }
}
