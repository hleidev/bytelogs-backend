package top.harrylei.forum.service.user.service.impl;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.service.user.converted.UserInfoConverter;
import top.harrylei.forum.service.user.repository.dao.UserInfoDAO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;
import top.harrylei.forum.service.user.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserInfoDAO userInfoDAO;

    @Override
    public BaseUserInfoDTO getUserInfoById(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            UserInfoDO userInfo = userInfoDAO.getByUserId(userId);
            if (userInfo == null) {
                return null;
            }
            return UserInfoConverter.toDTO(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息异常: userId={}", userId, e);
            return null;
        }
    }

    @Override
    public void getUserInfoAsync(Long userId, Consumer<BaseUserInfoDTO> callback) {
        if (callback == null) {
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                BaseUserInfoDTO userInfo = getUserInfoById(userId);
                callback.accept(userInfo);
            } catch (Exception e) {
                log.error("异步获取用户信息异常: userId={}", userId, e);
                callback.accept(null);
            }
        });
    }
}
