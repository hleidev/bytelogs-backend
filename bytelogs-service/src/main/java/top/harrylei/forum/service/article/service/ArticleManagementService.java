package top.harrylei.forum.service.article.service;

import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.enums.article.ArticleStatusTypeEnum;
import top.harrylei.forum.api.enums.article.PublishStatusEnum;

import java.util.List;

/**
 * 文章管理接口类
 *
 * @author harry
 */
public interface ArticleManagementService {

    /**
     * 审核文章（支持单个和批量）
     *
     * @param articleIds 文章ID列表（单个文章传单元素列表）
     * @param status     审核状态
     */
    void auditArticles(List<Long> articleIds, PublishStatusEnum status);

    /**
     * 删除文章（支持单个和批量）
     *
     * @param articleIds 文章ID列表（单个文章传单元素列表）
     */
    void deleteArticles(List<Long> articleIds);

    /**
     * 恢复文章（支持单个和批量）
     *
     * @param articleIds 文章ID列表（单个文章传单元素列表）
     */
    void restoreArticles(List<Long> articleIds);

    /**
     * 批量更新文章属性标识（置顶/加精/官方）
     *
     * @param articleIds 文章ID列表
     * @param statusType 状态类型
     * @param status    是否启用
     */
    void updateArticleProperty(List<Long> articleIds, ArticleStatusTypeEnum statusType, YesOrNoEnum status);
}
