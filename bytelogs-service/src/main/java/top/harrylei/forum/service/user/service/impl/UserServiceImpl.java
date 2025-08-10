package top.harrylei.forum.service.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.forum.api.enums.ErrorCodeEnum;
import top.harrylei.forum.api.enums.ResultCode;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.enums.user.UserRoleEnum;
import top.harrylei.forum.api.enums.user.UserStatusEnum;
import top.harrylei.forum.api.exception.BusinessException;
import top.harrylei.forum.api.model.auth.UserCreateReq;
import top.harrylei.forum.api.model.page.PageVO;
import top.harrylei.forum.api.model.page.param.UserQueryParam;
import top.harrylei.forum.api.model.user.dto.UserDetailDTO;
import top.harrylei.forum.api.model.user.dto.UserInfoDTO;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.core.util.BCryptUtil;
import top.harrylei.forum.core.util.PageUtils;
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
    private final RedisUtil redisUtil;
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
        if (userInfoDTO == null) {
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
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_MISSING, "用户ID");
        ExceptionUtil.requireValid(oldPassword, ErrorCodeEnum.PARAM_MISSING, "旧密码");
        ExceptionUtil.requireValid(newPassword, ErrorCodeEnum.PARAM_MISSING, "新密码");

        if (Objects.equals(oldPassword, newPassword)) {
            ExceptionUtil.error(ErrorCodeEnum.USER_UPDATE_FAILED, "新密码与旧密码相同");
        }

        // 校验旧密码
        UserDO user = userDAO.getUserById(userId);
        ExceptionUtil.requireValid(user, ErrorCodeEnum.USER_NOT_EXISTS, "userId=" + userId);

        if (BCryptUtil.notMatches(oldPassword, user.getPassword())) {
            ExceptionUtil.error(ErrorCodeEnum.USER_PASSWORD_ERROR);
        }

        resetPassword(userId, newPassword);

        authService.logout(userId);
    }

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatar 用户头像
     */
    @Override
    public void updateAvatar(Long userId, String avatar) {
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_MISSING, "用户ID");
        ExceptionUtil.requireValid(avatar, ErrorCodeEnum.PARAM_MISSING, "用户头像");

        UserInfoDTO userInfo = getUserInfoById(userId);

        redisUtil.del(RedisKeyConstants.getUserInfoKey(userInfo.getUserId()));
        userCacheService.updateUserInfoCache(userInfo);

        try {
            userInfo.setAvatar(avatar);
            userInfoDAO.updateById(userStructMapper.toDO(userInfo));
            log.info("用户头像更新成功: userId={}", userId);
        } catch (Exception e) {
            ExceptionUtil.error(ErrorCodeEnum.USER_UPDATE_FAILED, "用户头像更新失败，请稍候重试！", e);
        }
    }

    @Override
    public PageVO<UserDetailDTO> pageQuery(UserQueryParam queryParam) {
        ExceptionUtil.requireValid(queryParam, ErrorCodeEnum.PARAM_MISSING, "请求参数");

        try {
            IPage<UserDetailDTO> page = PageUtils.of(queryParam);
            IPage<UserDetailDTO> result = userDAO.pageUsers(queryParam, page);
            return PageUtils.from(result);
        } catch (Exception e) {
            ExceptionUtil.error(ErrorCodeEnum.SYSTEM_ERROR, "查询用户列表异常", e);
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
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_MISSING, "用户ID");

        try {
            return userDAO.getUserDetail(userId);
        } catch (Exception e) {
            ExceptionUtil.error(ErrorCodeEnum.SYSTEM_ERROR, "查询用户详细信息异常", e);
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
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_MISSING, "用户ID");
        ExceptionUtil.requireValid(status, ErrorCodeEnum.PARAM_MISSING, "用户状态");

        UserDO user = userDAO.getById(userId);
        ExceptionUtil.requireValid(user, ErrorCodeEnum.USER_NOT_EXISTS, "userId=" + userId);

        if (Objects.equals(status.getCode(), user.getStatus())) {
            log.warn("用户状态未改变，无需更新");
        }

        try {
            user.setStatus(status.getCode());
            userDAO.updateById(user);
            log.info("更新用户状态成功: userId={}", userId);
        } catch (Exception e) {
            ExceptionUtil.error(ErrorCodeEnum.USER_UPDATE_FAILED, "用户状态更新失败，请稍后重试", e);
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
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_MISSING, "用户ID");
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_MISSING, "密码");
        // 校验新密码安全性
        if (PasswordUtil.isInvalid(password)) {
            ExceptionUtil.error(ErrorCodeEnum.USER_PASSWORD_INVALID);
        }

        UserDO user = userDAO.getUserById(userId);
        ExceptionUtil.requireValid(user, ErrorCodeEnum.USER_NOT_EXISTS, "userId=" + userId);

        try {
            user.setPassword(BCryptUtil.encode(password));
            userDAO.updateById(user);
            log.info("用户密码更新成功: userId={}", userId);
        } catch (Exception e) {
            ExceptionUtil.error(ErrorCodeEnum.USER_UPDATE_FAILED, "用户密码更新失败，请稍后重试！", e);
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
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_MISSING, "用户ID");
        ExceptionUtil.requireValid(status, ErrorCodeEnum.PARAM_MISSING, "删除状态");

        UserDO user = userDAO.getById(userId);
        ExceptionUtil.requireValid(user, ErrorCodeEnum.USER_NOT_EXISTS, "userId=" + userId);

        UserInfoDO userInfo = userInfoDAO.getById(userId);
        ExceptionUtil.requireValid(userInfo, ErrorCodeEnum.USER_INFO_NOT_EXISTS, "userId=" + userId);

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
            ExceptionUtil.error(ErrorCodeEnum.USER_DELETE_FAILED, e);
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
        ExceptionUtil.requireValid(userId, ErrorCodeEnum.PARAM_MISSING, "用户ID");
        ExceptionUtil.requireValid(role, ErrorCodeEnum.PARAM_MISSING, "角色");

        UserInfoDO userInfo = userInfoDAO.getByUserId(userId);
        ExceptionUtil.requireValid(userInfo, ErrorCodeEnum.USER_INFO_NOT_EXISTS, "userId=" + userId);

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
            ExceptionUtil.error(ErrorCodeEnum.USER_UPDATE_FAILED, "更新用户角色失败 userId=" + userId, e);
        }
    }

    /**
     * 新建用户账号
     *
     * @param req 新建用户的请求参数
     */
    @Override
    public void save(UserCreateReq req) {
        ExceptionUtil.requireValid(req, ErrorCodeEnum.PARAM_MISSING, "请求参数");

        ExceptionUtil.requireValid(req.getRole(), ErrorCodeEnum.PARAM_VALIDATE_FAILED, "角色代码异常");

        Long operatorId = ReqInfoContext.getContext().getUserId();

        try {
            authService.register(req.getUsername(), req.getPassword(), req.getRole());
            log.info("新建用户账号成功: username={}, operatorId={}", req.getUsername(), operatorId);
        } catch (Exception e) {
            ExceptionUtil.error(ErrorCodeEnum.UNEXPECT_ERROR, "更新用户角色失败");
        }
    }
}
