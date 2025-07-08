package top.harrylei.forum.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVersionVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.article.converted.ArticleStructMapper;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;
import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.forum.service.article.service.ArticleDetailService;
import top.harrylei.forum.service.article.service.ArticleService;
import top.harrylei.forum.service.article.service.ArticleVersionService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文章版本管理服务实现
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleVersionServiceImpl implements ArticleVersionService {

    private final ArticleService articleService;
    private final ArticleDetailService articleDetailService;
    private final ArticleStructMapper articleStructMapper;

    @Override
    public List<ArticleVersionVO> getVersionHistory(Long articleId) {
        // 1. 验证文章存在性和权限
        ArticleDO article = articleService.getArticleById(articleId);
        validateAuthorPermission(article.getUserId());

        // 2. 获取所有版本
        List<ArticleDetailDO> allVersions = articleDetailService.getVersionHistory(articleId);

        // 3. 转换为VO并返回
        return allVersions.stream()
                .map(articleStructMapper::toVersionVO)
                .collect(Collectors.toList());
    }

    /**
     * 验证作者权限（仅作者本人和管理员可访问）
     */
    private void validateAuthorPermission(Long authorId) {
        Long currentUserId = getCurrentUserId();
        boolean isAdmin = isCurrentUserAdmin();
        boolean isAuthor = Objects.equals(authorId, currentUserId);

        ExceptionUtil.errorIf(!isAdmin && !isAuthor, ErrorCodeEnum.FORBID_ERROR_MIXED, "无权限查看该文章的版本历史");
    }

    private Long getCurrentUserId() {
        return ReqInfoContext.getContext().getUserId();
    }

    private boolean isCurrentUserAdmin() {
        return ReqInfoContext.getContext().isAdmin();
    }
}