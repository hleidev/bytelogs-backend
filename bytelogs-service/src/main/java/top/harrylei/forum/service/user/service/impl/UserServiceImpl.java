package top.harrylei.forum.service.user.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.service.user.converted.UserInfoStructMapper;
import top.harrylei.forum.service.user.repository.dao.UserInfoDAO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;
import top.harrylei.forum.service.user.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserInfoDAO userInfoDAO;
    private final UserInfoStructMapper userInfoStructMapper;

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
            return userInfoStructMapper.toDTO(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息异常: userId={}", userId, e);
            return null;
        }
    }
}
