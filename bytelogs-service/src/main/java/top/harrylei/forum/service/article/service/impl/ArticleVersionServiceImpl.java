package top.harrylei.forum.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVersionVO;
import top.harrylei.forum.api.model.vo.article.vo.TagSimpleVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.article.converted.ArticleStructMapper;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;
import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.forum.service.article.service.ArticleDetailService;
import top.harrylei.forum.service.article.service.ArticleService;
import top.harrylei.forum.service.article.service.ArticleTagService;
import top.harrylei.forum.service.article.service.ArticleVersionService;

import java.util.Collections;
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
    private final ArticleTagService articleTagService;

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

    @Override
    public ArticleVO getVersionDetail(Long articleId, Integer version) {
        // 1. 验证文章存在性和权限
        ArticleDO article = articleService.getArticleById(articleId);
        validateAuthorPermission(article.getUserId());

        // 2. 获取指定版本详情
        ArticleDetailDO detail = articleDetailService.getByArticleIdAndVersion(articleId, version);
        ExceptionUtil.requireValid(detail,
                                   ErrorCodeEnum.ARTICLE_NOT_EXISTS,
                                   "版本不存在 articleId=" + articleId + " version=" + version);

        // 3. 构建完整的文章VO
        ArticleVO articleVO = articleStructMapper.buildArticleVO(article, detail);

        // 4. 填充标签信息
        fillSingleArticleTags(articleVO);

        return articleVO;
    }

    /**
     * 填充单个文章标签信息
     */
    private void fillSingleArticleTags(ArticleVO article) {
        if (article == null) {
            return;
        }

        try {
            // 查询单个文章的标签信息
            List<TagSimpleVO> tags = articleTagService.listTagSimpleVoByArticleIds(List.of(article.getId()));

            // 清理标签中的articleId字段
            tags.forEach(tag -> tag.setArticleId(null));
            article.setTags(tags);
        } catch (Exception e) {
            log.error("填充文章标签信息失败", e);
            // 标签填充失败不应该影响主要数据的返回
            article.setTags(Collections.emptyList());
        }
    }

    private Long getCurrentUserId() {
        return ReqInfoContext.getContext().getUserId();
    }

    private boolean isCurrentUserAdmin() {
        return ReqInfoContext.getContext().isAdmin();
    }
}