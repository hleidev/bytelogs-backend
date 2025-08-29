package top.harrylei.community.service.statistics.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.harrylei.community.core.common.constans.RedisKeyConstants;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.RedisUtil;
import top.harrylei.community.service.statistics.repository.dao.ReadCountDAO;
import top.harrylei.community.service.statistics.service.ReadCountService;

import java.time.Duration;

/**
 * 文章统计服务实现类
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
    public void incrementReadCount(Long articleId) {
        String lockKey = buildReadLockKey(articleId);
        Duration duration = getLockExpiration();

        try {
            if (redisUtil.setIfAbsent(lockKey, "1", duration)) {
                readCountDAO.incrementReadCount(articleId);
                log.debug("文章阅读量统计成功: articleId={}", articleId);
            } else {
                log.debug("重复访问，跳过统计: articleId={}", articleId);
            }
        } catch (Exception e) {
            log.error("文章阅读量统计失败: articleId={}", articleId, e);
        }
    }

    @Override
    public Long getReadCount(Long articleId) {
        return readCountDAO.getReadCount(articleId);
    }

    @Override
    public void incrementPraiseCount(Long articleId) {
        readCountDAO.incrementPraiseCount(articleId);
        log.debug("文章点赞量增加成功: articleId={}", articleId);
    }

    @Override
    public void decrementPraiseCount(Long articleId) {
        readCountDAO.decrementPraiseCount(articleId);
        log.debug("文章点赞量减少成功: articleId={}", articleId);
    }

    @Override
    public Long getPraiseCount(Long articleId) {
        return readCountDAO.getPraiseCount(articleId);
    }

    @Override
    public void incrementCollectCount(Long articleId) {
        readCountDAO.incrementCollectCount(articleId);
        log.debug("文章收藏量增加成功: articleId={}", articleId);
    }

    @Override
    public void decrementCollectCount(Long articleId) {
        readCountDAO.decrementCollectCount(articleId);
        log.debug("文章收藏量减少成功: articleId={}", articleId);
    }

    @Override
    public Long getCollectCount(Long articleId) {
        return readCountDAO.getCollectCount(articleId);
    }

    @Override
    public void incrementCommentCount(Long articleId) {
        readCountDAO.incrementCommentCount(articleId);
        log.debug("文章评论量增加成功: articleId={}", articleId);
    }

    @Override
    public void decrementCommentCount(Long articleId) {
        readCountDAO.decrementCommentCount(articleId);
        log.debug("文章评论量减少成功: articleId={}", articleId);
    }

    @Override
    public Long getCommentCount(Long articleId) {
        return readCountDAO.getCommentCount(articleId);
    }

    /**
     * 构建阅读防重复锁Key
     */
    private String buildReadLockKey(Long articleId) {
        if (ReqInfoContext.getContext().isLoggedIn()) {
            // 登录用户：精确到用户
            Long userId = ReqInfoContext.getContext().getUserId();
            return RedisKeyConstants.getArticleReadCountLockKey(articleId, userId.toString(), "user");
        } else {
            // 未登录用户：按IP粗粒度控制
            String ip = ReqInfoContext.getContext().getClientIp();
            return RedisKeyConstants.getArticleReadCountLockKey(articleId, ip, "ip");
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