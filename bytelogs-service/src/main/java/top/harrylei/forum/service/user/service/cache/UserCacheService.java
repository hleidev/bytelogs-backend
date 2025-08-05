package top.harrylei.forum.service.user.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;
import top.harrylei.forum.core.util.RedisUtil;
import top.harrylei.forum.service.user.converted.UserStructMapper;
import top.harrylei.forum.service.user.repository.dao.UserInfoDAO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;
import top.harrylei.forum.core.util.JwtUtil;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户缓存服务
 *
 * @author harry
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
    public UserInfoDetailDTO getUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }

        // 1. 尝试从上下文获取
        UserInfoDetailDTO contextUser = ReqInfoContext.getContext().getUser();
        if (contextUser != null && contextUser.getUserId().equals(userId)) {
            return contextUser;
        }

        // 2. 尝试从缓存获取
        UserInfoDetailDTO userInfoDTO =
                redisUtil.get(RedisKeyConstants.getUserInfoKey(userId), UserInfoDetailDTO.class);

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
     * @param userId      用户ID
     * @param userInfoDTO 用户信息DTO
     */
    public void cacheUserInfo(Long userId, UserInfoDetailDTO userInfoDTO) {
        if (userId == null || userInfoDTO == null) {
            return;
        }

        try {
            redisUtil.set(RedisKeyConstants.getUserInfoKey(userId),
                          userInfoDTO,
                          Duration.ofSeconds(jwtUtil.getExpireSeconds()));
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
    public void updateUserInfoCache(UserInfoDetailDTO userInfoDTO) {
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

    /**
     * 批量获取用户信息，优先从缓存获取
     *
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    public List<UserInfoDetailDTO> listUserInfosByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        // 1. 构建缓存Key列表
        List<String> cacheKeys = userIds.stream().map(RedisKeyConstants::getUserInfoKey).toList();

        // 2. 批量从Redis获取
        Map<String, UserInfoDetailDTO> cachedUsers = redisUtil.mGet(cacheKeys, UserInfoDetailDTO.class);

        // 3. 一次遍历收集缓存命中和未命中的用户
        Map<Long, UserInfoDetailDTO> userMap = new HashMap<>();
        List<Long> missedUserIds = new ArrayList<>();

        for (int i = 0; i < userIds.size(); i++) {
            Long userId = userIds.get(i);
            String cacheKey = cacheKeys.get(i);
            UserInfoDetailDTO cachedUser = cachedUsers.get(cacheKey);
            if (cachedUser != null) {
                userMap.put(userId, cachedUser);
            } else {
                missedUserIds.add(userId);
            }
        }

        // 4. 从数据库查询未命中的用户
        if (!missedUserIds.isEmpty()) {
            log.debug("缓存未命中用户数: {}, userIds: {}", missedUserIds.size(), missedUserIds);

            List<UserInfoDO> dbUsers = userInfoDAO.listByUserIds(missedUserIds);
            List<UserInfoDetailDTO> dbUserList = dbUsers.stream()
                    .map(userStructMapper::toDTO)
                    .toList();

            // 5. 异步批量缓存数据库查询结果
            if (!dbUserList.isEmpty()) {
                asyncBatchCacheUserInfo(dbUserList);
            }

            // 6. 添加数据库查询的用户到结果Map
            dbUserList.forEach(user -> userMap.put(user.getUserId(), user));
        }

        // 7. 按原始顺序返回结果
        return userIds.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 异步批量缓存用户信息
     *
     * @param userInfoList 用户信息列表
     */
    private void asyncBatchCacheUserInfo(List<UserInfoDetailDTO> userInfoList) {
        // 使用虚拟线程异步执行，不阻塞主流程
        Thread.startVirtualThread(() -> {
            try {
                Map<String, UserInfoDetailDTO> cacheMap = userInfoList.stream()
                        .collect(Collectors.toMap(user -> RedisKeyConstants.getUserInfoKey(user.getUserId()),
                                                  user -> user));

                redisUtil.mSet(cacheMap, Duration.ofSeconds(jwtUtil.getExpireSeconds()));
                log.debug("批量缓存用户信息完成: count={}", userInfoList.size());
            } catch (Exception e) {
                log.error("批量缓存用户信息失败", e);
            }
        });
    }
}