package top.harrylei.forum.service.article.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.vo.article.dto.ArticleDTO;
import top.harrylei.forum.api.model.vo.article.vo.ArticleDetailVO;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.api.model.vo.article.vo.CategorySimpleVO;
import top.harrylei.forum.api.model.vo.article.vo.TagSimpleVO;
import top.harrylei.forum.api.model.vo.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.article.converted.ArticleStructMapper;
import top.harrylei.forum.service.article.repository.dao.ArticleDAO;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;
import top.harrylei.forum.service.article.service.*;
import top.harrylei.forum.service.user.converted.UserStructMapper;
import top.harrylei.forum.service.user.service.cache.UserCacheService;

/**
 * 文章服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final TransactionTemplate transactionTemplate;
    private final ArticleDAO articleDAO;
    private final ArticleStructMapper articleStructMapper;
    private final ArticleDetailService articleDetailService;
    private final ArticleTagService articleTagService;
    private final TagService tagService;
    private final CategoryService categoryService;
    private final UserStructMapper userStructMapper;
    private final UserCacheService userCacheService;

    /**
     * 保存文章
     *
     * @param articleDTO 文章传输对象
     * @return 文章ID
     */
    @Override
    public Long saveArticle(ArticleDTO articleDTO) {
        ArticleDO article = articleStructMapper.toDO(articleDTO);
        return transactionTemplate.execute(status -> {
            Long articleId;
            articleId = insertArticle(article, articleDTO.getContent(), articleDTO.getTagIds());
            log.info("新建文章成功 title={}", article.getTitle());
            return articleId;
        });
    }

    /**
     * 编辑文章
     *
     * @param articleDTO 文章传输对象
     * @param editor 编辑用户ID
     * @return 文章VO
     */
    @Override
    public ArticleVO updateArticle(ArticleDTO articleDTO, Long editor) {
        Long articleId = articleDTO.getId();
        ExceptionUtil.requireNonNull(articleId, ErrorCodeEnum.PARAM_ERROR, "文章ID不能为空");

        // 权限校验并获取原作者ID
        Long author = checkArticleEditPermission(articleId, editor);

        articleDTO.setUserId(author);
        ArticleDO articleDO = articleStructMapper.toDO(articleDTO);

        ArticleDTO article = transactionTemplate
            .execute(status -> updateArticle(articleDO, articleDTO.getContent(), articleDTO.getTagIds()));

        ArticleVO result = articleStructMapper.toVO(article);

        List<TagSimpleVO> tagSimpleList = tagService.listSimpleTagsByTagsIds(articleDTO.getTagIds());
        result.setTags(tagSimpleList);

        CategorySimpleVO categorySimple = categoryService.getSimpleCategoryByCategoryId(articleDTO.getCategoryId());
        result.setCategory(categorySimple);

        log.info("编辑文章成功 editor={} articleId={}", editor, articleDTO.getId());
        return result;
    }

    /**
     * 删除文章
     *
     * @param articleId 文章ID
     * @param operatorId 操作者ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteArticle(Long articleId, Long operatorId) {
        checkArticleEditPermission(articleId, operatorId);

        updateArticleDeletedStatus(articleId, YesOrNoEnum.YES);
        log.info("删除文章成功 articleId={} operatorId={}", articleId, operatorId);
    }

    /**
     * 恢复文章
     *
     * @param articleId 文章ID
     * @param operatorId 操作者ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void restoreArticle(Long articleId, Long operatorId) {
        checkArticleEditPermission(articleId, operatorId, true);

        updateArticleDeletedStatus(articleId, YesOrNoEnum.NO);
        log.info("恢复文章成功 articleId={} operatorId={}", articleId, operatorId);
    }

    /**
     * 文章详细
     *
     * @param articleId 文章ID
     * @return 文章详细展示对象
     */
    @Override
    public ArticleDetailVO getArticleDetail(Long articleId) {
        ArticleDTO dto = getCompleteArticle(articleId);

        UserInfoDetailDTO user = userCacheService.getUserInfo(dto.getUserId());

        checkArticleViewPermission(dto);

        return new ArticleDetailVO().setArticle(articleStructMapper.toVO(dto)).setAuthor(userStructMapper.toVO(user));
    }

    private void checkArticleViewPermission(ArticleDTO article) {
        // 已删除文章 or 草稿或审核中文章仅作者和管理员可见
        if (YesOrNoEnum.YES.equals(article.getDeleted()) || !PublishStatusEnum.PUBLISHED.equals(article.getStatus())) {
            boolean isAdmin = ReqInfoContext.getContext().isAdmin();
            boolean isAuthor = Objects.equals(article.getUserId(), ReqInfoContext.getContext().getUserId());
            ExceptionUtil.errorIf(!isAdmin && !isAuthor, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "文章不存在");
        }
    }

    /**
     * 检查文章编辑权限
     *
     * @param articleId 文章ID
     * @param editorId 编辑者ID
     * @return 文章原作者ID
     */
    private Long checkArticleEditPermission(Long articleId, Long editorId) {
        return checkArticleEditPermission(articleId, editorId, false);
    }

    /**
     * 检查文章编辑权限
     *
     * @param articleId 文章ID
     * @param editorId 编辑者ID
     * @return 文章原作者ID
     */
    private Long checkArticleEditPermission(Long articleId, Long editorId, Boolean includeDeleted) {
        // 检查文章是否存在
        Long authorId;
        if (includeDeleted) {
            authorId = articleDAO.getUserIdByArticleIdIncludeDeleted(articleId);
        } else {
            authorId = articleDAO.getUserIdByArticleId(articleId);
        }
        ExceptionUtil.requireNonNull(authorId, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "articleId=" + articleId);

        // 只有作者本人或管理员可以修改文章
        boolean isAdmin = ReqInfoContext.getContext().isAdmin();
        boolean isAuthor = Objects.equals(authorId, editorId);
        ExceptionUtil.errorIf(!isAuthor && !isAdmin, ErrorCodeEnum.FORBID_ERROR_MIXED, "当前用户非管理员，无权限修改非自己我的文章");

        return authorId;
    }

    private void updateArticleDeletedStatus(Long articleId, YesOrNoEnum deletedStatus) {
        articleDAO.updateDeleted(articleId, deletedStatus.getCode());

        if (YesOrNoEnum.YES.equals(deletedStatus)) {
            articleDetailService.deleteByArticleId(articleId);
            articleTagService.deleteByArticleId(articleId);
        } else {
            articleDetailService.restoreByArticleId(articleId);
            articleTagService.restoreByArticleId(articleId);
        }
    }

    private Long insertArticle(ArticleDO article, String content, List<Long> tagIds) {
        if (needToReview(article)) {
            article.setStatus(PublishStatusEnum.REVIEW.getCode());
        }
        Long articleId = articleDAO.insertArticle(article);
        articleDetailService.saveArticleContent(articleId, content);

        if (tagIds != null && !tagIds.isEmpty()) {
            articleTagService.saveBatch(articleId, tagIds);
        }

        return articleId;
    }

    private boolean needToReview(ArticleDO article) {
        if (ReqInfoContext.getContext().isAdmin()) {
            return false;
        }
        // TODO 添加用户白名单
        return Objects.equals(article.getStatus(), PublishStatusEnum.PUBLISHED.getCode());
    }

    private ArticleDTO updateArticle(ArticleDO article, String content, List<Long> tagIds) {
        if (needToReview(article)) {
            article.setStatus(PublishStatusEnum.PUBLISHED.getCode());
        }

        articleDAO.updateById(article);

        articleDetailService.updateArticleContent(article.getId(), content);
        articleTagService.updateTags(article.getId(), tagIds);

        return getCompleteArticle(article.getId());
    }

    /**
     * 获取完整的文章信息，包括内容和标签
     * 
     * @param articleId 文章ID
     * @return 完整的文章DTO
     */
    private ArticleDTO getCompleteArticle(Long articleId) {
        ArticleDO article = articleDAO.getById(articleId);
        ExceptionUtil.requireNonNull(article, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "articleId=" + articleId);

        ArticleDTO result = articleStructMapper.toDTO(article);

        result.setContent(articleDetailService.getContentByArticleId(articleId));
        result.setTagIds(articleTagService.listTagIdsByArticleId(articleId));

        return result;
    }
}
