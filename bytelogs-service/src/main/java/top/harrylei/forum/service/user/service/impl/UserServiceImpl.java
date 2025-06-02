package top.harrylei.forum.service.user.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.user.converted.UserInfoStructMapper;
import top.harrylei.forum.service.user.repository.dao.UserDAO;
import top.harrylei.forum.service.user.repository.dao.UserInfoDAO;
import top.harrylei.forum.service.user.repository.entity.UserDO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;
import top.harrylei.forum.service.user.service.UserService;

/**
 * 用户服务实现类 提供用户信息查询和更新功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserInfoDAO userInfoDAO;
    private final UserInfoStructMapper userInfoStructMapper;
    private final UserDAO userDAO;

    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息DTO，不存在则返回null
     */
    @Override
    public BaseUserInfoDTO getUserInfoById(Long userId) {
        ExceptionUtil.requireNonNull(userId, StatusEnum.PARAM_MISSING, "用户ID为空");

        try {
            UserInfoDO userInfo = userInfoDAO.getByUserId(userId);
            if (userInfo == null) {
                return null;
            }
            return userInfoStructMapper.toDTO(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息异常: userId={}", userId, e);
            return null;
        }
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
            UserInfoDO userInfo = userInfoStructMapper.toDO(userInfoDTO);
            userInfoDAO.updateById(userInfo);

            // 更新用户账户信息
            UserDO userDO = new UserDO();
            userDO.setId(userInfo.getUserId());
            userDO.setUserName(userInfo.getUserName());
            userDAO.updateById(userDO);

            // 更新请求上下文中的用户信息
            ReqInfoContext.getContext().setUser(userInfoDTO);
            log.info("用户信息更新成功: userId={}", userInfoDTO.getUserId());
        } catch (Exception e) {
            log.error("更新用户数据失败: userId={}", ReqInfoContext.getContext().getUserId(), e);
            ExceptionUtil.error(StatusEnum.USER_UPDATE_FAILED, "用户信息更新失败，请稍后重试");
        }
    }
}
