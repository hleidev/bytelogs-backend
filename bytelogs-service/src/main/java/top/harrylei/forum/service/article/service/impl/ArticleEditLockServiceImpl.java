package top.harrylei.forum.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;
import top.harrylei.forum.core.util.RedisUtil;
import top.harrylei.forum.service.article.service.ArticleEditLockService;

import java.time.Duration;
import java.util.UUID;

/**
 * 文章编辑锁服务实现
 *
 * @author harry
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleEditLockServiceImpl implements ArticleEditLockService {

    private final RedisUtil redisUtil;

    /**
     * 编辑锁过期时间：30分钟
     */
    private static final Duration EDIT_LOCK_EXPIRE_TIME = Duration.ofMinutes(30);

    /**
     * 获取文章编辑锁
     *
     * @param articleId 文章ID
     * @param userId 用户ID
     * @return 编辑令牌，获取失败返回null
     */
    @Override
    public String getEditLock(Long articleId, Long userId) {
        if (articleId == null || userId == null) {
            log.warn("获取编辑锁参数无效：articleId={}, userId={}", articleId, userId);
            return null;
        }

        String lockKey = getLockKey(articleId);

        try {
            // 检查是否已被占用
            String existingToken = redisUtil.get(lockKey, String.class);
            if (existingToken != null) {
                log.debug("文章{}已被占用，无法获取编辑锁", articleId);
                return null;
            }

            // 生成新的编辑令牌
            String editToken = UUID.randomUUID().toString();

            // 存储编辑锁
            boolean success = redisUtil.set(lockKey, editToken, EDIT_LOCK_EXPIRE_TIME);
            if (success) {
                log.info("用户{}成功获取文章{}的编辑锁", userId, articleId);
                return editToken;
            } else {
                log.warn("用户{}获取文章{}编辑锁失败：Redis操作失败", userId, articleId);
                return null;
            }

        } catch (Exception e) {
            log.error("获取文章{}编辑锁异常，用户：{}", articleId, userId, e);
            return null;
        }
    }

    private static @NotNull String getLockKey(Long articleId) {
        return RedisKeyConstants.getArticleEditLockKey(articleId);
    }

    /**
     * 验证编辑令牌
     *
     * @param articleId 文章ID
     * @param editToken 编辑令牌
     * @return 是否有效
     */
    @Override
    public boolean validateEditToken(Long articleId, String editToken) {
        if (articleId == null || editToken == null || editToken.trim().isEmpty()) {
            log.debug("验证编辑令牌参数无效：articleId={}, editToken={}", articleId, editToken);
            return false;
        }

        try {
            String lockKey = getLockKey(articleId);
            String storedToken = redisUtil.get(lockKey, String.class);

            boolean isValid = editToken.equals(storedToken);

            if (!isValid) {
                log.debug("文章{}编辑令牌验证失败，提供的令牌与存储令牌不匹配", articleId);
            }

            return isValid;

        } catch (Exception e) {
            log.error("验证文章{}编辑令牌异常", articleId, e);
            return false;
        }
    }

    /**
     * 刷新编辑令牌过期时间
     *
     * @param articleId 文章ID
     * @param editToken 编辑令牌
     * @return 是否刷新成功
     */
    @Override
    public boolean refreshEditToken(Long articleId, String editToken) {
        if (!validateEditToken(articleId, editToken)) {
            log.debug("刷新编辑令牌失败：令牌验证不通过，文章：{}", articleId);
            return false;
        }

        try {
            String lockKey = getLockKey(articleId);

            // 刷新过期时间
            boolean refreshed = redisUtil.expire(lockKey, EDIT_LOCK_EXPIRE_TIME);

            if (refreshed) {
                log.debug("刷新文章{}编辑锁成功", articleId);
            } else {
                log.warn("刷新文章{}编辑锁失败：Redis操作失败", articleId);
            }

            return refreshed;

        } catch (Exception e) {
            log.error("刷新文章{}编辑锁异常", articleId, e);
            return false;
        }
    }

    /**
     * 释放编辑锁
     *
     * @param articleId 文章ID
     * @param editToken 编辑令牌
     * @return 是否释放成功
     */
    @Override
    public boolean releaseEditLock(Long articleId, String editToken) {
        if (!validateEditToken(articleId, editToken)) {
            log.debug("释放编辑锁失败：令牌验证不通过，文章：{}", articleId);
            return false;
        }

        try {
            String lockKey = getLockKey(articleId);

            // 删除编辑锁
            boolean released = redisUtil.delete(lockKey);

            if (released) {
                log.info("成功释放文章{}的编辑锁", articleId);
            } else {
                log.warn("释放文章{}编辑锁失败：Redis操作失败", articleId);
            }

            return released;

        } catch (Exception e) {
            log.error("释放文章{}编辑锁异常", articleId, e);
            return false;
        }
    }

}