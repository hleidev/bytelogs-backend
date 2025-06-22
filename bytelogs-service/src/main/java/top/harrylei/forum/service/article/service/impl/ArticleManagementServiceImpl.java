package top.harrylei.forum.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.article.service.ArticleManagementService;
import top.harrylei.forum.service.article.service.ArticleService;

import java.util.Objects;

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
     * @param articleId 文章ID
     * @param status    审核状态
     */
    @Override
    public void auditArticle(Long articleId, PublishStatusEnum status) {
        validateAuditStatus(status);

        articleService.updateArticleStatus(articleId, status);
        log.info("审核文章成功 status={}, operatorId={}", status, articleId);
    }

    private void validateAuditStatus(PublishStatusEnum status) {
        if (!Objects.equals(status, PublishStatusEnum.PUBLISHED) &&
                !Objects.equals(status, PublishStatusEnum.REJECTED)) {
            ExceptionUtil.error(ErrorCodeEnum.PARAM_VALIDATE_FAILED, "无效的审核状态，只支持通过(1)或驳回(3)");
        }
    }
}
