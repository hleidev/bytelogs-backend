package top.harrylei.forum.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.article.vo.ArticleVO;
import top.harrylei.forum.api.model.article.vo.ArticleVersionVO;
import top.harrylei.forum.api.model.article.vo.TagSimpleVO;
import top.harrylei.forum.api.model.article.vo.VersionDiffVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.core.util.DiffUtil;
import top.harrylei.forum.service.article.converted.ArticleStructMapper;
import top.harrylei.forum.service.article.repository.dao.ArticleDetailDAO;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;
import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.forum.service.article.service.ArticleQueryService;
import top.harrylei.forum.service.article.service.ArticleTagService;
import top.harrylei.forum.service.article.service.ArticleVersionService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 文章版本管理服务实现
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleVersionServiceImpl implements ArticleVersionService {

    private final ArticleQueryService articleQueryService;
    private final ArticleDetailDAO articleDetailDAO;
    private final ArticleStructMapper articleStructMapper;
    private final ArticleTagService articleTagService;

    @Override
    public List<ArticleVersionVO> getVersionHistory(Long articleId) {
        // 1. 验证文章存在性
        ArticleDO article = articleQueryService.getArticleById(articleId);

        // 2. 权限校验：只有作者和管理员可以访问版本管理功能
        validateAuthorPermission(article.getUserId());

        // 3. 获取所有版本（作者和管理员可以看到所有版本）
        List<ArticleDetailDO> allVersions = articleDetailDAO.getVersionHistory(articleId);

        // 4. 转换为VO并返回
        return allVersions.stream()
                .map(articleStructMapper::toVersionVO)
                .toList();
    }

    private void validateAuthorPermission(Long authorId) {
        Long currentUserId = getCurrentUserId();
        boolean isAdmin = isCurrentUserAdmin();
        boolean isAuthor = Objects.equals(authorId, currentUserId);

        ExceptionUtil.errorIf(!isAdmin && !isAuthor, ErrorCodeEnum.FORBID_ERROR_MIXED, "无权限访问版本管理功能");
    }

    @Override
    public ArticleVO getVersionDetail(Long articleId, Integer version) {
        // 1. 验证文章存在性
        ArticleDO article = articleQueryService.getArticleById(articleId);

        // 2. 权限校验：只有作者和管理员可以查看版本详情
        validateAuthorPermission(article.getUserId());

        // 3. 获取指定版本详情
        ArticleDetailDO detail = getArticleVersion(articleId, version);

        // 4. 构建完整的文章VO
        ArticleVO articleVO = articleStructMapper.buildArticleVO(article, detail);

        // 5. 填充标签信息
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

    @Override
    public VersionDiffVO compareVersions(Long articleId, Integer version1, Integer version2) {
        // 1. 验证文章存在性
        ArticleDO article = articleQueryService.getArticleById(articleId);

        // 2. 权限校验
        validateAuthorPermission(article.getUserId());

        // 3. 获取两个版本的详情
        ArticleDetailDO detail1 = getArticleVersion(articleId, version1);
        ArticleDetailDO detail2 = getArticleVersion(articleId, version2);

        // 4. 执行对比
        return buildVersionDiff(detail1, detail2);
    }

    /**
     * 获取版本并进行存在性检查
     */
    private ArticleDetailDO getArticleVersion(Long articleId, Integer version) {
        ArticleDetailDO detail = articleDetailDAO.getByArticleIdAndVersion(articleId, version);
        ExceptionUtil.requireValid(detail, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "请求的版本不存在，请检查版本号是否正确");
        return detail;
    }

    /**
     * 构建版本对比结果
     */
    private VersionDiffVO buildVersionDiff(ArticleDetailDO detail1, ArticleDetailDO detail2) {
        return new VersionDiffVO()
                .setVersion1(articleStructMapper.toVersionVO(detail1))
                .setVersion2(articleStructMapper.toVersionVO(detail2))
                .setTitleDiff(DiffUtil.diff(detail1.getTitle(), detail2.getTitle()))
                .setContentDiff(DiffUtil.diff(detail1.getContent(), detail2.getContent()))
                .setSummaryDiff(DiffUtil.diff(detail1.getSummary(), detail2.getSummary()));
    }

    private Long getCurrentUserId() {
        return ReqInfoContext.getContext().getUserId();
    }

    private boolean isCurrentUserAdmin() {
        return ReqInfoContext.getContext().isAdmin();
    }
}