package top.harrylei.forum.service.auth.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.user.LoginTypeEnum;
import top.harrylei.forum.api.model.exception.ExceptionUtil;
import top.harrylei.forum.api.model.vo.constants.StatusEnum;
import top.harrylei.forum.core.util.BCryptUtil;
import top.harrylei.forum.core.util.PasswordUtil;
import top.harrylei.forum.service.auth.service.AuthService;
import top.harrylei.forum.service.user.repository.dao.UserDAO;
import top.harrylei.forum.service.user.repository.entity.UserDO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;
import top.harrylei.forum.service.util.JwtUtil;

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
        UserDO existingUser = userDAO.getUserByUserName(username);
        if (existingUser != null) {
            throw ExceptionUtil.of(StatusEnum.USER_LOGIN_NAME_REPEAT, username);
        }

        // 创建新用户
        UserDO user = new UserDO();
        user.setUserName(username);
        user.setPassword(BCryptUtil.hash(password));
        user.setThirdAccountId("");
        user.setLoginType(LoginTypeEnum.USER_PWD.getCode());
        userDAO.saveUser(user);

        // 创建用户信息
        UserInfoDO userInfo = new UserInfoDO();
        userInfo.setUserId(user.getId());
        userInfo.setUserName(username);
        userInfo.setPhoto("");
        userDAO.save(userInfo);

        // 更新上下文并返回token
        Long userId = user.getId();
        // TODO 用户上下文保存 userId
        log.info("用户注册成功: {}", username);
        
        return true;
    }

    @Override
    public String login(String username, String password) {
        // 参数校验
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "用户名或密码不能为空");
        }

        // 查找用户
        UserDO user = userDAO.getUserByUserName(username);
        if (user == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "用户不存在: " + username);
        }

        // 校验密码
        if (!BCryptUtil.matches(password, user.getPassword())) {
            log.warn("用户密码错误: {}", username);
            throw ExceptionUtil.of(StatusEnum.USER_PWD_ERROR);
        }

        // 更新上下文并返回token
        Long userId = user.getId();
        // TODO 用户上下文保存 userId
        log.info("用户登录成功: {}", username);
        
        return jwtUtil.generateToken(userId);
    }
}
