package top.harrylei.community.service.user.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.community.api.model.user.dto.UserInfoDTO;
import top.harrylei.community.core.common.constans.RedisKeyConstants;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.JwtUtil;
import top.harrylei.community.core.util.RedisUtil;
import top.harrylei.community.service.user.converted.UserStructMapper;
import top.harrylei.community.service.user.repository.dao.UserInfoDAO;
import top.harrylei.community.service.user.repository.entity.UserInfoDO;

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
     * 用户信息缓存过期时间：30分钟
     */
    private static final Duration USER_INFO_CACHE_EXPIRE = Duration.ofMinutes(30);

    /**
     * 用户信息查询分布式锁超时时间
     */
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(30);

    /**
     * 获取用户信息，优先从缓存获取，使用分布式锁防止缓存击穿
     *
     * @param userId 用户ID
     * @return 用户信息DTO，若不存在则返回null
     */
    public UserInfoDTO getUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }

        // 1. 尝试从上下文获取
        UserInfoDTO contextUser = ReqInfoContext.getContext().getUser();
        if (contextUser != null && contextUser.getUserId().equals(userId)) {
            return contextUser;
        }

        // 2. 尝试从缓存获取
        UserInfoDTO userInfoDTO = redisUtil.get(RedisKeyConstants.getUserInfoKey(userId), UserInfoDTO.class);
        if (userInfoDTO != null) {
            log.debug("缓存命中: userId={}", userId);
            return userInfoDTO;
        }

        // 3. 缓存未命中，查询数据库并使用分布式锁防止缓存击穿
        return processDatabaseQuery(userId);
    }

    private UserInfoDTO processDatabaseQuery(Long userId) {
        UserInfoDTO userInfoDTO;
        String lockKey = RedisKeyConstants.getDistributedLockKey("user_info:" + userId);

        try {
            // 尝试获取分布式锁
            boolean lock = redisUtil.setIfAbsent(lockKey, "1", LOCK_TIMEOUT);
            if (lock) {
                try {
                    // 获得锁后再次检查缓存
                    userInfoDTO = redisUtil.get(RedisKeyConstants.getUserInfoKey(userId), UserInfoDTO.class);
                    if (userInfoDTO != null) {
                        log.debug("获取锁后缓存命中: userId={}", userId);
                        return userInfoDTO;
                    }

                    // 从数据库查询
                    log.debug("缓存未命中，从数据库查询: userId={}", userId);
                    UserInfoDO userInfoDO = userInfoDAO.getById(userId);
                    if (userInfoDO == null) {
                        log.warn("用户信息不存在: userId={}", userId);
                        return null;
                    }

                    // 转换并缓存
                    userInfoDTO = userStructMapper.toDTO(userInfoDO);
                    cacheUserInfo(userId, userInfoDTO);

                    return userInfoDTO;
                } finally {
                    // 释放分布式锁
                    redisUtil.del(lockKey);
                }
            } else {
                // 未获得锁，等待后重试
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }

                // 递归重试获取用户信息
                return getUserInfo(userId);
            }
        } catch (Exception e) {
            log.error("获取用户信息异常: userId={}", userId, e);
            // 兜底直接查询数据库
            UserInfoDO userInfoDO = userInfoDAO.getById(userId);
            return userInfoDO != null ? userStructMapper.toDTO(userInfoDO) : null;
        }
    }

    /**
     * 缓存用户信息
     *
     * @param userId      用户ID
     * @param userInfoDTO 用户信息DTO
     */
    public void cacheUserInfo(Long userId, UserInfoDTO userInfoDTO) {
        if (userId == null || userInfoDTO == null) {
            return;
        }

        try {
            redisUtil.set(RedisKeyConstants.getUserInfoKey(userId), userInfoDTO, USER_INFO_CACHE_EXPIRE);
            log.debug("用户信息已缓存: userId={}, 过期时间: {}分钟", userId, USER_INFO_CACHE_EXPIRE.toMinutes());
        } catch (Exception e) {
            log.error("缓存用户信息失败: userId={}", userId, e);
        }
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
    public List<UserInfoDTO> listUserInfosByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        // 1. 构建缓存Key列表
        List<String> cacheKeys = userIds.stream().map(RedisKeyConstants::getUserInfoKey).toList();

        // 2. 批量从Redis获取
        Map<String, UserInfoDTO> cachedUsers = redisUtil.mGet(cacheKeys, UserInfoDTO.class);

        // 3. 一次遍历收集缓存命中和未命中的用户
        Map<Long, UserInfoDTO> userMap = new HashMap<>();
        List<Long> missedUserIds = new ArrayList<>();

        for (int i = 0; i < userIds.size(); i++) {
            Long userId = userIds.get(i);
            String cacheKey = cacheKeys.get(i);
            UserInfoDTO cachedUser = cachedUsers.get(cacheKey);
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
            List<UserInfoDTO> dbUserList = dbUsers.stream()
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
    private void asyncBatchCacheUserInfo(List<UserInfoDTO> userInfoList) {
        // 使用虚拟线程异步执行，不阻塞主流程
        Thread.startVirtualThread(() -> {
            try {
                Map<String, UserInfoDTO> cacheMap = userInfoList.stream()
                        .collect(Collectors.toMap(user -> RedisKeyConstants.getUserInfoKey(user.getUserId()),
                                                  user -> user));

                redisUtil.mSet(cacheMap, USER_INFO_CACHE_EXPIRE);
                log.debug("批量缓存用户信息完成: count={}, 过期时间: {}分钟",
                          userInfoList.size(), USER_INFO_CACHE_EXPIRE.toMinutes());
            } catch (Exception e) {
                log.error("批量缓存用户信息失败", e);
            }
        });
    }
}