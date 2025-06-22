package top.harrylei.forum.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.article.service.ArticleManagementService;
import top.harrylei.forum.service.article.service.ArticleService;

import java.util.List;

/**
 * 文章管理实现类
 *
 * @author Harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleManagementServiceImpl implements ArticleManagementService {

    private final ArticleService articleService;

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
                articleService.updateArticleStatus(articleId, status);
                log.info("审核文章成功 articleId={} status={} operatorId={}", articleId, status, operatorId);
            } catch (Exception e) {
                log.error("审核文章失败 articleId={} status={} operatorId={} error={}",
                          articleId, status, operatorId, e.getMessage(), e);
                // 继续处理其他文章，不因单个失败而中断
            }
        }

        log.info("批量审核完成 total={} status={} operatorId={}", articleIds.size(), status, operatorId);
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