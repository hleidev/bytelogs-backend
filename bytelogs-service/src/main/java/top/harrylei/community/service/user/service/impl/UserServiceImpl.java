package top.harrylei.community.service.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.community.api.enums.ResultCode;
import top.harrylei.community.api.enums.YesOrNoEnum;
import top.harrylei.community.api.enums.user.UserRoleEnum;
import top.harrylei.community.api.enums.user.UserStatusEnum;
import top.harrylei.community.api.exception.BusinessException;
import top.harrylei.community.api.model.auth.UserCreateReq;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.api.model.page.param.UserQueryParam;
import top.harrylei.community.api.model.user.dto.UserDetailDTO;
import top.harrylei.community.api.model.user.dto.UserInfoDTO;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.BCryptUtil;
import top.harrylei.community.core.util.PageUtils;
import top.harrylei.community.core.util.PasswordUtil;
import top.harrylei.community.service.auth.service.AuthService;
import top.harrylei.community.service.user.converted.UserStructMapper;
import top.harrylei.community.service.user.repository.dao.UserDAO;
import top.harrylei.community.service.user.repository.dao.UserInfoDAO;
import top.harrylei.community.service.user.repository.entity.UserDO;
import top.harrylei.community.service.user.repository.entity.UserInfoDO;
import top.harrylei.community.service.user.service.UserService;
import top.harrylei.community.service.user.service.cache.UserCacheService;

import java.util.Objects;

/**
 * 用户服务实现类
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserInfoDAO userInfoDAO;
    private final UserStructMapper userStructMapper;
    private final UserDAO userDAO;
    private final AuthService authService;
    private final UserCacheService userCacheService;

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID，不能为空
     * @return 用户信息DTO
     * @throws BusinessException 当用户ID为空或用户不存在时抛出
     */
    @Override
    public UserInfoDTO getUserInfoById(Long userId) {
        // 参数校验
        if (userId == null) {
            ResultCode.INVALID_PARAMETER.throwException("用户ID不能为空");
        }

        UserInfoDTO userInfo = userCacheService.getUserInfo(userId);
        if (userInfo == null) {
            ResultCode.USER_NOT_EXISTS.throwException();
        }

        return userInfo;
    }

    /**
     * 更新用户信息
     *
     * @param userInfoDTO 需要更新的用户信息DTO，不能为空
     * @throws BusinessException 当参数无效或用户不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserInfoDTO userInfoDTO) {
        // 参数校验
        if (userInfoDTO == null && userInfoDTO.getUserId() == null) {
            ResultCode.INVALID_PARAMETER.throwException("用户信息不能为空");
        }

        Long userId = userInfoDTO.getUserId();
        if (userId == null) {
            ResultCode.INVALID_PARAMETER.throwException("用户ID不能为空");
        }

        // 校验用户存在性
        UserDO userDO = userDAO.getById(userId);
        if (userDO == null) {
            ResultCode.USER_NOT_EXISTS.throwException();
        }

        // 转换并更新
        UserInfoDO userInfo = userStructMapper.toDO(userInfoDTO);

        // 原子性更新用户基本信息和详细信息
        userDO.setUserName(userInfo.getUserName());
        userDAO.updateById(userDO);
        userInfoDAO.updateById(userInfo);

        // 事务成功后清理缓存
        userCacheService.clearUserInfoCache(userId);

        log.info("用户信息更新成功: userId={}", userId);
    }

    /**
     * 更新用户密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null) {
            ResultCode.INVALID_PARAMETER.throwException("用户ID不能为空");
        }
        if (oldPassword == null || newPassword == null) {
            ResultCode.INVALID_PARAMETER.throwException("旧密码或新密码不能为空");
        }

        if (Objects.equals(oldPassword, newPassword)) {
            ResultCode.USER_NOT_EXISTS.throwException("新旧密码不能相同");
        }

        // 校验旧密码
        UserDO user = userDAO.getUserById(userId);
        if (user == null) {
            ResultCode.USER_NOT_EXISTS.throwException();
        }

        if (BCryptUtil.notMatches(oldPassword, user.getPassword())) {
            ResultCode.AUTHENTICATION_FAILED.throwException("旧密码不正确");
        }

        resetPassword(userId, newPassword);

        authService.logout(userId);
    }

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatar 用户头像URL
     */
    @Override
    public void updateAvatar(Long userId, String avatar) {
        if (userId == null) {
            ResultCode.INVALID_PARAMETER.throwException("用户ID不能为空");
        }
        if (avatar == null || avatar.trim().isEmpty()) {
            ResultCode.INVALID_PARAMETER.throwException("用户头像不能为空");
        }

        // 获取用户信息并校验用户存在性
        UserInfoDTO userInfo = getUserInfoById(userId);

        // 更新头像信息
        userInfo.setAvatar(avatar.trim());
        userInfoDAO.updateById(userStructMapper.toDO(userInfo));

        // 删除缓存，确保数据一致性
        userCacheService.clearUserInfoCache(userId);

        log.info("用户头像更新成功: userId={}", userId);
    }

    @Override
    public PageVO<UserDetailDTO> pageQuery(UserQueryParam queryParam) {
        if (queryParam == null) {
            ResultCode.INVALID_PARAMETER.throwException();
        }

        try {
            IPage<UserDetailDTO> page = PageUtils.of(queryParam);
            IPage<UserDetailDTO> result = userDAO.pageUsers(queryParam, page);
            return PageUtils.from(result);
        } catch (Exception e) {
            log.error("查询用户列表异常: ", e);
            ResultCode.INTERNAL_ERROR.throwException();
            return null;
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
        if (userId == null) {
            ResultCode.INVALID_PARAMETER.throwException();
        }

        try {
            return userDAO.getUserDetail(userId);
        } catch (Exception e) {
            ResultCode.INTERNAL_ERROR.throwException();
            return null;
        }
    }

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 新状态
     */
    @Override
    public void updateStatus(Long userId, UserStatusEnum status) {
        if (userId == null) {
            ResultCode.INVALID_PARAMETER.throwException();
        }
        if (status == null) {
            ResultCode.INVALID_PARAMETER.throwException();
        }

        UserDO user = userDAO.getById(userId);
        if (user == null) {
            ResultCode.USER_NOT_EXISTS.throwException();
        }

        if (Objects.equals(status.getCode(), user.getStatus())) {
            log.warn("用户状态未改变，无需更新");
        }

        try {
            user.setStatus(status.getCode());
            userDAO.updateById(user);
            log.info("更新用户状态成功: userId={}", userId);
        } catch (Exception e) {
            ResultCode.INTERNAL_ERROR.throwException();
        }
    }

    /**
     * 重置密码
     *
     * @param userId   用户ID
     * @param password 新密码
     */
    @Override
    public void resetPassword(Long userId, String password) {
        if (userId == null) {
            ResultCode.INVALID_PARAMETER.throwException();
        }
        if (password == null) {
            ResultCode.INVALID_PARAMETER.throwException();
        }
        // 校验新密码安全性
        if (PasswordUtil.isInvalid(password)) {
            ResultCode.AUTH_PASSWORD_INVALID.throwException();
        }

        UserDO user = userDAO.getUserById(userId);
        if (user == null) {
            ResultCode.USER_NOT_EXISTS.throwException();
        }

        try {
            user.setPassword(BCryptUtil.encode(password));
            userDAO.updateById(user);
            log.info("用户密码更新成功: userId={}", userId);
        } catch (Exception e) {
            ResultCode.INTERNAL_ERROR.throwException();
        }
    }

    /**
     * 更新删除状态
     *
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateDeleted(Long userId, YesOrNoEnum status) {
        if (userId == null) {
            ResultCode.INVALID_PARAMETER.throwException();
        }
        if (status == null) {
            ResultCode.INVALID_PARAMETER.throwException();
        }

        UserDO user = userDAO.getById(userId);
        if (user == null) {
            ResultCode.USER_NOT_EXISTS.throwException();
        }

        UserInfoDO userInfo = userInfoDAO.getById(userId);
        if (userInfo == null) {
            ResultCode.USER_NOT_EXISTS.throwException();
        }

        if (Objects.equals(user.getDeleted(), status.getCode())) {
            log.warn("用户删除状态未变更，无需更新");
            return;
        }

        Long operatorId = ReqInfoContext.getContext().getUserId();

        try {
            user.setDeleted(status.getCode());
            userDAO.updateById(user);
            userInfo.setDeleted(status.getCode());
            userInfoDAO.updateById(userInfo);
            log.info("用户删除状态更新成功: userId={}, deleted={}, operatorId={}",
                    userId,
                    status.getLabel(),
                    operatorId);
        } catch (Exception e) {
            ResultCode.INTERNAL_ERROR.throwException();
        }
    }

    /**
     * 更新用户角色
     *
     * @param userId 用户ID
     * @param role   角色枚举
     */
    @Override
    public void updateUserRole(Long userId, UserRoleEnum role) {
        if (userId == null) {
            ResultCode.INVALID_PARAMETER.throwException();
        }
        if (role == null) {
            ResultCode.INVALID_PARAMETER.throwException();
        }

        UserInfoDO userInfo = userInfoDAO.getByUserId(userId);
        if (userInfo == null) {
            ResultCode.USER_NOT_EXISTS.throwException();
        }

        Long operatorId = ReqInfoContext.getContext().getUserId();

        // 判断新旧角色是否一致，避免无效写入
        if (Objects.equals(userInfo.getUserRole(), role.getCode())) {
            log.warn("用户角色未变更，无需更新");
        }

        try {
            userInfo.setUserRole(role.getCode());
            userInfoDAO.updateById(userInfo);
            log.info("更新用户角色成功: userId={}, role={}, operatorId={}", userId, role.getLabel(), operatorId);
        } catch (Exception e) {
            ResultCode.INTERNAL_ERROR.throwException();
        }
    }

    /**
     * 新建用户账号
     *
     * @param req 新建用户的请求参数
     */
    @Override
    public void save(UserCreateReq req) {
        if (req == null) {
            ResultCode.INVALID_PARAMETER.throwException();
        }

        if (req.getRole() == null) {
            ResultCode.INVALID_PARAMETER.throwException();
        }

        Long operatorId = ReqInfoContext.getContext().getUserId();

        try {
            authService.register(req.getUsername(), req.getPassword(), req.getRole());
            log.info("新建用户账号成功: username={}, operatorId={}", req.getUsername(), operatorId);
        } catch (Exception e) {
            ResultCode.INTERNAL_ERROR.throwException();
        }
    }
}
