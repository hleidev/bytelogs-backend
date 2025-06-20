package top.harrylei.forum.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.vo.article.dto.ArticleDTO;
import top.harrylei.forum.api.model.vo.article.vo.ArticleDetailVO;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.api.model.vo.page.Page;
import top.harrylei.forum.api.model.vo.page.PageHelper;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.article.converted.ArticleStructMapper;
import top.harrylei.forum.service.article.repository.dao.ArticleDAO;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;
import top.harrylei.forum.service.article.service.ArticleDetailService;
import top.harrylei.forum.service.article.service.ArticleService;
import top.harrylei.forum.service.article.service.ArticleTagService;
import top.harrylei.forum.service.user.converted.UserStructMapper;
import top.harrylei.forum.service.user.service.cache.UserCacheService;

import java.util.List;
import java.util.Objects;

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
     * @return 文章VO
     */
    @Override
    public ArticleVO updateArticle(ArticleDTO articleDTO) {
        Long articleId = articleDTO.getId();
        ExceptionUtil.requireNonNull(articleId, ErrorCodeEnum.PARAM_ERROR, "文章ID不能为空");

        // 权限校验并获取原作者ID
        Long author = checkArticleEditPermission(articleId);

        articleDTO.setUserId(author);
        ArticleDO articleDO = articleStructMapper.toDO(articleDTO);

        ArticleVO article = transactionTemplate
                .execute(status -> updateArticle(articleDO, articleDTO.getContent(), articleDTO.getTagIds()));

        log.info("编辑文章成功 editor={} articleId={}", ReqInfoContext.getContext().getUserId(), articleDTO.getId());
        return article;
    }

    /**
     * 删除文章
     *
     * @param articleId 文章ID
     */
    @Override
    public void deleteArticle(Long articleId) {
        checkArticleEditPermission(articleId);

        updateArticleDeletedStatus(articleId, YesOrNoEnum.YES);
        log.info("删除文章成功 articleId={} operatorId={}", articleId, ReqInfoContext.getContext().getUserId());
    }

    /**
     * 恢复文章
     *
     * @param articleId 文章ID
     */
    @Override
    public void restoreArticle(Long articleId) {
        checkArticleEditPermission(articleId, true);

        updateArticleDeletedStatus(articleId, YesOrNoEnum.NO);
        log.info("恢复文章成功 articleId={} operatorId={}", articleId, ReqInfoContext.getContext().getUserId());
    }

    /**
     * 文章详细
     *
     * @param articleId 文章ID
     * @return 文章详细展示对象
     */
    @Override
    public ArticleDetailVO getArticleDetail(Long articleId) {
        ArticleVO completeArticleVO = getCompleteArticleVO(articleId);

        // 权限检查：已删除文章 or 草稿或审核中文章仅作者和管理员可见
        if (YesOrNoEnum.YES.equals(completeArticleVO.getDeleted()) || !PublishStatusEnum.PUBLISHED.equals(
                completeArticleVO.getStatus())) {
            boolean isAdmin = ReqInfoContext.getContext().isAdmin();
            boolean isAuthor = Objects.equals(completeArticleVO.getUserId(), ReqInfoContext.getContext().getUserId());
            ExceptionUtil.errorIf(!isAdmin && !isAuthor, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "文章不存在");
        }

        UserInfoDetailDTO user = userCacheService.getUserInfo(completeArticleVO.getUserId());

        return new ArticleDetailVO().setArticle(completeArticleVO).setAuthor(userStructMapper.toVO(user));
    }

    /**
     * 更新状态
     *
     * @param status 修改状态
     */
    @Override
    public void updateArticleStatus(Long articleId, PublishStatusEnum status) {
        ArticleDO articleDO = articleDAO.getByArticleId(articleId);
        ExceptionUtil.requireNonNull(articleDO, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "文章不存在或已被删除");

        // 检查目标状态与原状态是否相同，相同则无需更新
        if (Objects.equals(articleDO.getStatus(), status.getCode())) {
            log.info("文章状态未变更，无需更新 articleId={} status={}", articleId, status);
            return;
        }

        // 权限校验
        checkArticleEditPermission(articleId);

        if (needToReview(status)) {
            status = PublishStatusEnum.REVIEW;
        }

        // 如果审核后的最终状态与数据库当前状态一致，也无需更新
        if (Objects.equals(articleDO.getStatus(), status.getCode())) {
            log.info("文章状态经审核校验后未变更，无需更新 articleId={} status={}", articleId, status);
            return;
        }

        // 执行最终状态更新
        int updated = articleDAO.updateStatus(articleId, status.getCode());
        ExceptionUtil.errorIf(updated == 0, ErrorCodeEnum.SYSTEM_ERROR, "更新文章状态失败");

        log.info("文章状态更新成功 articleId={} status={} operatorId={}", articleId, status,
                 ReqInfoContext.getContext().getUserId());
    }

    /**
     * 分页查询
     *
     * @param req 分页请求参数
     * @return 分页查询结果
     */
    @Override
    public PageVO<ArticleDTO> page(Page req) {
        List<ArticleDO> articleDOList = articleDAO.listArticle(req.getLimitSql());
        Long total = articleDAO.countArticle();

        List<ArticleDTO> result = articleDOList.stream().filter(Objects::nonNull).map(articleStructMapper::toDTO).toList();
        return PageHelper.build(result, req.getPageNum(), req.getPageSize(), total);
    }


    /**
     * 检查文章编辑权限
     *
     * @param articleId      文章ID
     * @param includeDeleted 是否包含已删除的文章
     * @return 文章原作者ID
     */
    private Long checkArticleEditPermission(Long articleId, Boolean includeDeleted) {
        // 获取当前操作者ID
        Long operatorId = ReqInfoContext.getContext().getUserId();
        boolean isAdmin = ReqInfoContext.getContext().isAdmin();

        // 检查文章是否存在
        Long authorId;
        if (includeDeleted) {
            authorId = articleDAO.getUserIdByArticleIdIncludeDeleted(articleId);
        } else {
            authorId = articleDAO.getUserIdByArticleId(articleId);
        }
        ExceptionUtil.requireNonNull(authorId, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "articleId=" + articleId);

        // 只有作者本人或管理员可以修改文章
        boolean isAuthor = Objects.equals(authorId, operatorId);
        ExceptionUtil.errorIf(!isAuthor && !isAdmin,
                              ErrorCodeEnum.FORBID_ERROR_MIXED,
                              "当前用户非管理员，无权限修改非自己的文章");

        return authorId;
    }

    /**
     * 检查文章编辑权限 - 不包含已删除的文章
     *
     * @param articleId 文章ID
     * @return 文章原作者ID
     */
    private Long checkArticleEditPermission(Long articleId) {
        return checkArticleEditPermission(articleId, false);
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
        if (needToReview(article.getStatus())) {
            article.setStatus(PublishStatusEnum.REVIEW.getCode());
        }
        Long articleId = articleDAO.insertArticle(article);
        articleDetailService.saveArticleContent(articleId, content);

        if (tagIds != null && !tagIds.isEmpty()) {
            articleTagService.saveBatch(articleId, tagIds);
        }

        return articleId;
    }

    private boolean needToReview(Integer status) {
        ExceptionUtil.requireNonNull(status, ErrorCodeEnum.PARAM_MISSING, "状态码");
        return needToReview(PublishStatusEnum.fromCode(status));
    }

    private boolean needToReview(PublishStatusEnum status) {
        if (ReqInfoContext.getContext().isAdmin()) {
            return false;
        }
        // TODO 添加用户白名单
        return Objects.equals(status, PublishStatusEnum.PUBLISHED);
    }

    private ArticleVO updateArticle(ArticleDO article, String content, List<Long> tagIds) {
        if (needToReview(article.getStatus())) {
            article.setStatus(PublishStatusEnum.PUBLISHED.getCode());
        }

        articleDAO.updateById(article);

        articleDetailService.updateArticleContent(article.getId(), content);
        articleTagService.updateTags(article.getId(), tagIds);

        return getCompleteArticleVO(article.getId());
    }

    /**
     * 获取完整的文章VO（一次查询获取所有展示数据）
     *
     * @param articleId 文章ID
     * @return 完整的文章VO
     */
    private ArticleVO getCompleteArticleVO(Long articleId) {
        // 使用联表查询一次性获取完整的ArticleVO
        ArticleVO result = articleDAO.getArticleVOByArticleId(articleId);
        ExceptionUtil.requireNonNull(result, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "articleId=" + articleId);

        return result;
    }
}
