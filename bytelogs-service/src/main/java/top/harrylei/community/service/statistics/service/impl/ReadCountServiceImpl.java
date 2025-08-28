package top.harrylei.community.service.statistics.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.harrylei.community.api.enums.comment.ContentTypeEnum;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.RedisUtil;
import top.harrylei.community.service.statistics.repository.dao.ReadCountDAO;
import top.harrylei.community.service.statistics.service.ReadCountService;

import java.time.Duration;

/**
 * 阅读统计服务实现类
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReadCountServiceImpl implements ReadCountService {

    private final ReadCountDAO readCountDAO;
    private final RedisUtil redisUtil;

    @Override
    @Async("statisticsExecutor")
    public void incrementReadCount(Long contentId, ContentTypeEnum contentType) {
        String lockKey = buildLockKey(contentId, contentType.getCode());
        Duration duration = getLockExpiration();

        try {
            if (redisUtil.setIfAbsent(lockKey, "1", duration)) {
                readCountDAO.incrementReadCount(contentId, contentType.getCode());
                log.debug("阅读量统计成功: {}", lockKey);
            } else {
                log.debug("重复访问，跳过统计: {}", lockKey);
            }
        } catch (Exception e) {
            log.error("阅读量统计失败: contentId={}, contentType={}", contentId, contentType, e);
        }
    }

    @Override
    public Long getReadCount(Long contentId, ContentTypeEnum contentType) {
        return readCountDAO.getReadCount(contentId, contentType.getCode());
    }

    /**
     * 构建防重复锁Key
     */
    private String buildLockKey(Long contentId, Integer contentType) {
        if (ReqInfoContext.getContext().isLoggedIn()) {
            // 登录用户：精确到用户
            Long userId = ReqInfoContext.getContext().getUserId();
            return "read_lock:" + contentId + ":" + contentType + ":user:" + userId;
        } else {
            // 未登录用户：按IP粗粒度控制
            String ip = ReqInfoContext.getContext().getClientIp();
            return "read_lock:" + contentId + ":" + contentType + ":ip:" + ip;
        }
    }

    /**
     * 获取锁过期时间
     */
    private Duration getLockExpiration() {
        if (ReqInfoContext.getContext().isLoggedIn()) {
            return Duration.ofHours(24);
        } else {
            return Duration.ofMinutes(10);
        }
    }

}