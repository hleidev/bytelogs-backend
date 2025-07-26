package top.harrylei.forum.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.enums.ErrorCodeEnum;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.enums.article.ArticleStatusTypeEnum;
import top.harrylei.forum.api.enums.article.PublishStatusEnum;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.article.service.ArticleManagementService;
import top.harrylei.forum.service.article.service.ArticleCommandService;

import java.util.List;

/**
 * 文章管理实现类
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleManagementServiceImpl implements ArticleManagementService {

    private final ArticleCommandService articleCommandService;

    /**
     * 审核文章
     *
     * @param articleIds 文章ID列表（单个文章传单元素列表）
     * @param status     审核状态
     */
    @Override
    public void auditArticles(List<Long> articleIds, PublishStatusEnum status) {
        // 参数校验
        validateAuditStatus(status);

        Long operatorId = ReqInfoContext.getContext().getUserId();

        // 批量处理文章状态更新
        for (Long articleId : articleIds) {
            try {
                articleCommandService.updateArticleStatus(articleId, status);
            } catch (Exception e) {
                log.error("审核文章失败 articleId={} status={} operatorId={} error={}",
                          articleId, status, operatorId, e.getMessage(), e);
                // 继续处理其他文章，不因单个失败而中断
            }
        }

        log.info("批量审核完成 total={} status={} operatorId={}", articleIds.size(), status, operatorId);
    }

    /**
     * 删除文章
     *
     * @param articleIds 文章ID列表（单个文章传单元素列表）
     */
    @Override
    public void deleteArticles(List<Long> articleIds) {
        Long operatorId = ReqInfoContext.getContext().getUserId();

        // 批量处理文章删除
        for (Long articleId : articleIds) {
            try {
                articleCommandService.deleteArticle(articleId);
            } catch (Exception e) {
                log.error("删除文章失败 articleId={} operatorId={} error={}",
                          articleId, operatorId, e.getMessage(), e);
                // 继续处理其他文章，不因单个失败而中断
            }
        }

        log.info("批量删除完成 total={} operatorId={}", articleIds.size(), operatorId);
    }

    /**
     * 恢复文章（支持单个和批量）
     *
     * @param articleIds 文章ID列表（单个文章传单元素列表）
     */
    @Override
    public void restoreArticles(List<Long> articleIds) {
        Long operatorId = ReqInfoContext.getContext().getUserId();

        // 批量处理文章恢复
        for (Long articleId : articleIds) {
            try {
                articleCommandService.restoreArticle(articleId);
            } catch (Exception e) {
                log.error("恢复文章失败 articleId={} operatorId={} error={}",
                          articleId, operatorId, e.getMessage(), e);
                // 继续处理其他文章，不因单个失败而中断
            }
        }

        log.info("批量恢复完成 total={} operatorId={}", articleIds.size(), operatorId);
    }

    /**
     * 批量更新文章属性标识（置顶/加精/官方）
     *
     * @param articleIds 文章ID列表
     * @param statusType 状态类型
     * @param status    是否启用
     */
    @Override
    public void updateArticleProperty(List<Long> articleIds, ArticleStatusTypeEnum statusType, YesOrNoEnum status) {
        Long operatorId = ReqInfoContext.getContext().getUserId();

        // 批量处理文章属性更新
        for (Long articleId : articleIds) {
            try {
                articleCommandService.updateArticleProperty(articleId, statusType, status);
            } catch (Exception e) {
                log.error("更新文章{}属性失败 articleId={} enabled={} operatorId={} error={}",
                          statusType.name(), articleId, status, operatorId, e.getMessage(), e);
                // 继续处理其他文章，不因单个失败而中断
            }
        }

        log.info("批量更新文章{}属性完成 total={} enabled={} operatorId={}",
                 statusType.name(), articleIds.size(), status, operatorId);
    }

    private void validateAuditStatus(PublishStatusEnum status) {
        switch (status) {
            case PUBLISHED, REJECTED -> {
                // 有效状态，什么都不做
            }
            default -> ExceptionUtil.error(ErrorCodeEnum.PARAM_VALIDATE_FAILED,
                                           "无效的审核状态，只支持通过(PUBLISHED)或驳回(REJECTED)");
        }
    }
}