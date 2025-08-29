package top.harrylei.community.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.enums.article.ArticlePublishStatusEnum;
import top.harrylei.community.api.enums.article.CreamStatusEnum;
import top.harrylei.community.api.enums.article.OfficialStatusEnum;
import top.harrylei.community.api.enums.article.ToppingStatusEnum;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.service.article.service.ArticleCommandService;
import top.harrylei.community.service.article.service.ArticleManagementService;

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
    public void auditArticles(List<Long> articleIds, ArticlePublishStatusEnum status) {
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

    @Override
    public void updateArticleTopping(List<Long> articleIds, ToppingStatusEnum toppingStat) {
        Long operationId = ReqInfoContext.getContext().getUserId();

        // 批量处理文章置顶状态更新
        for (Long articleId : articleIds) {
            try {
                articleCommandService.updateArticleTopping(articleId, toppingStat);
            } catch (Exception e) {
                log.error("更新文章置顶状态失败 articleId={} toppingStat={} operatorId={} error={}",
                        articleId, toppingStat, operationId, e.getMessage(), e);
                // 继续处理其他文章，不因单个失败而中断
            }
        }
    }

    @Override
    public void updateArticleCream(List<Long> articleIds, CreamStatusEnum creamStat) {
        Long operationId = ReqInfoContext.getContext().getUserId();

        // 批量处理文章加精状态更新
        for (Long articleId : articleIds) {
            try {
                articleCommandService.updateArticleCream(articleId, creamStat);
            } catch (Exception e) {
                log.error("更新文章加精状态失败 articleId={} creamStat={} operatorId={} error={}",
                        articleId, creamStat, operationId, e.getMessage(), e);
                // 继续处理其他文章，不因单个失败而中断
            }
        }
    }

    @Override
    public void updateArticleOfficial(List<Long> articleIds, OfficialStatusEnum officialStat) {
        Long operationId = ReqInfoContext.getContext().getUserId();

        // 批量处理文章官方状态更新
        for (Long articleId : articleIds) {
            try {
                articleCommandService.updateArticleOfficial(articleId, officialStat);
            } catch (Exception e) {
                log.error("更新文章官方状态失败 articleId={} officialStat={} operatorId={} error={}",
                        articleId, officialStat, operationId, e.getMessage(), e);
                // 继续处理其他文章，不因单个失败而中断
            }
        }
    }

    private void validateAuditStatus(ArticlePublishStatusEnum status) {
        switch (status) {
            case PUBLISHED, REJECTED -> {
                // 有效状态，什么都不做
            }
            default -> ResultCode.INVALID_PARAMETER.throwException();
        }
    }
}