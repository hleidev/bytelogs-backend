package top.harrylei.forum.service.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.core.security.UserInfoService;
import top.harrylei.forum.service.user.converted.UserInfoConverter;
import top.harrylei.forum.service.user.repository.dao.UserDAO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;

import java.util.function.Consumer;

/**
 * 用户信息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {

    private final UserDAO userDAO;

    @Override
    public BaseUserInfoDTO getUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }

        try {
            UserInfoDO userInfo = userDAO.getById(userId);
            if (userInfo == null) {
                return null;
            }

            return UserInfoConverter.toDTO(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息异常: userId={}", userId, e);
            return null;
        }
    }

    @Async
    @Override
    public void loadUserInfo(Long userId, Consumer<BaseUserInfoDTO> callback) {
        try {
            BaseUserInfoDTO userInfo = getUserInfo(userId);
            if (callback != null) {
                callback.accept(userInfo);
            }
        } catch (Exception e) {
            log.error("异步加载用户信息异常: userId={}", userId, e);
            if (callback != null) {
                callback.accept(null);
            }
        }
    }
}