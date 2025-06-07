package top.harrylei.forum.service.user.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.common.RedisKeyConstants;
import top.harrylei.forum.core.util.RedisUtil;
import top.harrylei.forum.service.user.converted.UserStructMapper;
import top.harrylei.forum.service.user.repository.dao.UserInfoDAO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;
import top.harrylei.forum.core.util.JwtUtil;

/**
 * 用户缓存服务
 * <p>
 * 提供用户信息的缓存管理，包括获取、缓存和清除用户信息 作为解决循环依赖问题的共享服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final RedisUtil redisUtil;
    private final UserInfoDAO userInfoDAO;
    private final UserStructMapper userStructMapper;
    private final JwtUtil jwtUtil;

    /**
     * 获取用户信息，优先从缓存获取
     *
     * @param userId 用户ID
     * @return 用户信息DTO，若不存在则返回null
     */
    public BaseUserInfoDTO getUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }

        // 1. 尝试从上下文获取
        BaseUserInfoDTO contextUser = ReqInfoContext.getContext().getUser();
        if (contextUser != null && contextUser.getUserId().equals(userId)) {
            return contextUser;
        }

        // 2. 尝试从缓存获取
        BaseUserInfoDTO userInfoDTO =
                redisUtil.getObj(RedisKeyConstants.getUserInfoKey(userId), BaseUserInfoDTO.class);

        // 缓存命中，直接返回
        if (userInfoDTO != null) {
            log.debug("缓存命中: userId={}", userId);
            return userInfoDTO;
        }

        // 3. 缓存未命中，从数据库查询
        log.debug("缓存未命中，从数据库查询: userId={}", userId);
        UserInfoDO userInfoDO = userInfoDAO.getById(userId);
        if (userInfoDO == null) {
            log.warn("用户信息不存在: userId={}", userId);
            return null;
        }

        // 4. 转换并缓存
        userInfoDTO = userStructMapper.toDTO(userInfoDO);
        cacheUserInfo(userId, userInfoDTO);

        return userInfoDTO;
    }

    /**
     * 缓存用户信息
     *
     * @param userId 用户ID
     * @param userInfoDTO 用户信息DTO
     */
    public void cacheUserInfo(Long userId, BaseUserInfoDTO userInfoDTO) {
        if (userId == null || userInfoDTO == null) {
            return;
        }

        try {
            redisUtil.setObj(RedisKeyConstants.getUserInfoKey(userId), userInfoDTO, jwtUtil.getExpireSeconds());
            log.debug("用户信息已缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("缓存用户信息失败: userId={}", userId, e);
        }
    }

    /**
     * 更新缓存中的用户信息
     * 
     * @param userInfoDTO 更新后的用户信息
     */
    public void updateUserInfoCache(BaseUserInfoDTO userInfoDTO) {
        if (userInfoDTO == null || userInfoDTO.getUserId() == null) {
            return;
        }

        // 更新缓存
        cacheUserInfo(userInfoDTO.getUserId(), userInfoDTO);
    }

    /**
     * 清除用户信息缓存
     *
     * @param userId 用户ID
     */
    public void clearUserInfoCache(Long userId) {
        if (userId == null) {
            return;
        }

        try {
            redisUtil.del(RedisKeyConstants.getUserInfoKey(userId));
            log.debug("用户信息缓存已清除: userId={}", userId);
        } catch (Exception e) {
            log.error("清除用户信息缓存失败: userId={}", userId, e);
        }
    }
}