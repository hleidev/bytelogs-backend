package top.harrylei.forum.service.article.service;

/**
 * 文章编辑锁服务 - 防止并发编辑冲突
 *
 * @author harry
 */
public interface ArticleEditLockService {

    /**
     * 获取编辑锁
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return 编辑令牌，如果获取失败返回null
     */
    String getEditLock(Long articleId, Long userId);

    /**
     * 验证编辑令牌
     *
     * @param articleId 文章ID
     * @param editToken 编辑令牌
     * @return 是否有效
     */
    boolean validateEditToken(Long articleId, String editToken);

    /**
     * 刷新编辑令牌过期时间
     *
     * @param articleId 文章ID
     * @param editToken 编辑令牌
     * @return 是否刷新成功
     */
    boolean refreshEditToken(Long articleId, String editToken);

    /**
     * 释放编辑锁
     *
     * @param articleId 文章ID
     * @param editToken 编辑令牌
     * @return 是否释放成功
     */
    boolean releaseEditLock(Long articleId, String editToken);

}