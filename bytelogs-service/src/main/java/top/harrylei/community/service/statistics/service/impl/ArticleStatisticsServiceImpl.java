package top.harrylei.community.service.statistics.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.harrylei.community.api.model.statistics.dto.ArticleStatisticsDTO;
import top.harrylei.community.core.common.constans.RedisKeyConstants;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.RedisUtil;
import top.harrylei.community.service.statistics.converted.ArticleStatisticsStructMapper;
import top.harrylei.community.service.statistics.repository.dao.ArticleStatisticsDAO;
import top.harrylei.community.service.statistics.repository.entity.ArticleStatisticsDO;
import top.harrylei.community.service.statistics.service.ArticleStatisticsService;

import java.time.Duration;

/**
 * 文章统计服务实现类
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleStatisticsServiceImpl implements ArticleStatisticsService {

    private final ArticleStatisticsDAO articleStatisticsDAO;
    private final RedisUtil redisUtil;
    private final ArticleStatisticsStructMapper articleStatisticsStructMapper;

    @Override
    public ArticleStatisticsDTO getArticleStatistics(Long articleId) {
        ArticleStatisticsDO statistics = articleStatisticsDAO.getByArticleId(articleId);
        if (statistics == null) {
            return new ArticleStatisticsDTO();
        }

        return articleStatisticsStructMapper.toDTO(statistics);
    }

    @Override
    @Async("statisticsExecutor")
    public void incrementReadCount(Long articleId) {
        String lockKey = buildReadLockKey(articleId);
        Duration duration = getLockExpiration();

        try {
            if (redisUtil.setIfAbsent(lockKey, "1", duration)) {
                articleStatisticsDAO.incrementReadCount(articleId);
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
        return articleStatisticsDAO.getReadCount(articleId);
    }

    @Override
    public void incrementPraiseCount(Long articleId) {
        articleStatisticsDAO.incrementPraiseCount(articleId);
        log.debug("文章点赞量增加成功: articleId={}", articleId);
    }

    @Override
    public void decrementPraiseCount(Long articleId) {
        articleStatisticsDAO.decrementPraiseCount(articleId);
        log.debug("文章点赞量减少成功: articleId={}", articleId);
    }

    @Override
    public Long getPraiseCount(Long articleId) {
        return articleStatisticsDAO.getPraiseCount(articleId);
    }

    @Override
    public void incrementCollectCount(Long articleId) {
        articleStatisticsDAO.incrementCollectCount(articleId);
        log.debug("文章收藏量增加成功: articleId={}", articleId);
    }

    @Override
    public void decrementCollectCount(Long articleId) {
        articleStatisticsDAO.decrementCollectCount(articleId);
        log.debug("文章收藏量减少成功: articleId={}", articleId);
    }

    @Override
    public Long getCollectCount(Long articleId) {
        return articleStatisticsDAO.getCollectCount(articleId);
    }

    @Override
    public void incrementCommentCount(Long articleId) {
        articleStatisticsDAO.incrementCommentCount(articleId);
        log.debug("文章评论量增加成功: articleId={}", articleId);
    }

    @Override
    public void decrementCommentCount(Long articleId) {
        articleStatisticsDAO.decrementCommentCount(articleId);
        log.debug("文章评论量减少成功: articleId={}", articleId);
    }

    @Override
    public Long getCommentCount(Long articleId) {
        return articleStatisticsDAO.getCommentCount(articleId);
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