package top.harrylei.forum.service.article.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.vo.article.dto.ArticleDTO;
import top.harrylei.forum.api.model.vo.article.req.ArticleQueryParam;
import top.harrylei.forum.api.model.vo.article.vo.ArticleDetailVO;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.api.model.vo.article.vo.TagSimpleVO;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 *
 * @author Harry
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
     * @param userId    当前用户ID（可为null表示未登录）
     * @param isAdmin   是否为管理员
     * @return 文章详细展示对象
     */
    @Override
    public ArticleDetailVO getArticleDetail(Long articleId, Long userId, boolean isAdmin) {
        ArticleVO completeArticleVO = getCompleteArticleVO(articleId);

        // 权限检查：已删除文章 or 草稿或审核中文章仅作者和管理员可见
        if (YesOrNoEnum.YES.equals(completeArticleVO.getDeleted()) ||
                !PublishStatusEnum.PUBLISHED.equals(completeArticleVO.getStatus())) {
            boolean isAuthor = userId != null && Objects.equals(completeArticleVO.getUserId(), userId);
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
     * 分页查询文章（智能处理查询权限）
     *
     * @param queryParam 分页查询参数
     * @return 分页查询结果
     */
    @Override
    public PageVO<ArticleVO> pageQuery(ArticleQueryParam queryParam) {
        // 获取当前用户信息
        ReqInfoContext.ReqInfo reqInfo = ReqInfoContext.getContext();
        Long currentUserId = reqInfo.getUserId();
        boolean isAdmin = reqInfo.isAdmin();

        // 处理查询逻辑
        processQueryPermissions(queryParam, currentUserId, isAdmin);

        // 创建MyBatis-Plus分页对象
        IPage<ArticleVO> page = new Page<>(queryParam.getPageNum(), queryParam.getPageSize());

        // 第一步：分页查询文章基础信息（避免JOIN标签表导致的重复记录）
        queryParam.setTagIdList(queryParam.getTagIdList());
        IPage<ArticleVO> result = articleDAO.pageArticleVO(queryParam, page);

        // 第二步：批量查询标签信息并填充到结果中（提升性能，便于缓存）
        fillArticleTags(result.getRecords());

        // 使用PageHelper.build构建分页结果
        return PageHelper.build(result);
    }

    /**
     * 处理查询权限逻辑
     *
     * @param queryParam    查询参数
     * @param currentUserId 当前用户ID
     * @param isAdmin       是否为管理员
     */
    private void processQueryPermissions(ArticleQueryParam queryParam, Long currentUserId, boolean isAdmin) {
        // 1. 处理"只查询我的文章"逻辑
        if (Boolean.TRUE.equals(queryParam.getOnlyMine())) {
            // 需要登录才能查询自己的文章
            ExceptionUtil.requireNonNull(currentUserId, ErrorCodeEnum.UNAUTHORIZED);
            queryParam.setUserId(currentUserId);
        }

        // 2. 处理删除状态权限
        if (queryParam.getDeleted() != null && Objects.equals(queryParam.getDeleted(), YesOrNoEnum.YES)) {
            // 只有管理员才能查看已删除的文章
            ExceptionUtil.errorIf(!isAdmin, ErrorCodeEnum.FORBID_ERROR_MIXED, "无权限查看已删除文章");
        } else if (queryParam.getDeleted() == null && !isAdmin) {
            // 如果没有指定删除状态，非管理员默认只查询未删除的文章
            queryParam.setDeleted(YesOrNoEnum.NO);
        }

        // 3. 处理文章状态权限
        if (queryParam.getStatus() != null) {
            // 如果查询草稿或审核中的文章，需要是自己的文章或管理员
            if (queryParam.getStatus() == PublishStatusEnum.DRAFT || queryParam.getStatus() == PublishStatusEnum.REVIEW) {
                if (!isAdmin && !Objects.equals(queryParam.getUserId(), currentUserId)) {
                    // 非管理员且不是查询自己的文章时，不允许查询非发布状态的文章
                    ExceptionUtil.error(ErrorCodeEnum.FORBID_ERROR_MIXED, "无权限查看该状态的文章");
                }
            }
        } else {
            // 如果没有指定状态，未登录用户或非本人查询时只能看已发布的文章
            if (currentUserId == null || (queryParam.getUserId() != null && !Objects.equals(queryParam.getUserId(),
                                                                                            currentUserId) && !isAdmin)) {
                queryParam.setStatus(PublishStatusEnum.PUBLISHED);
            }
        }

        // 4. 处理用户ID权限校验
        if (queryParam.getUserId() != null && !isAdmin) {
            // 非管理员查询指定用户的文章时，只能查询已发布的文章
            if (!Objects.equals(queryParam.getUserId(), currentUserId)) {
                queryParam.setStatus(PublishStatusEnum.PUBLISHED);
                queryParam.setDeleted(YesOrNoEnum.NO);
            }
        }

        log.debug("查询参数处理完成: userId={}, onlyMine={}, status={}, deleted={}, currentUser={}, isAdmin={}",
                  queryParam.getUserId(), queryParam.getOnlyMine(), queryParam.getStatus(),
                  queryParam.getDeleted(), currentUserId, isAdmin);
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
        ArticleVO result = articleDAO.getArticleVoByArticleId(articleId);
        ExceptionUtil.requireNonNull(result, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "articleId=" + articleId);

        return result;
    }

    /**
     * 企业级标签填充：批量查询标签信息并填充到文章列表
     * <p>
     * 优势：
     * 1. 避免N+1查询问题
     * 2. 标签数据可独立缓存
     * 3. 便于性能监控和优化
     * 4. 支持异步处理
     *
     * @param articles 文章列表
     */
    private void fillArticleTags(List<ArticleVO> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }

        try {
            // 收集所有文章ID
            List<Long> articleIds = articles.stream()
                    .map(ArticleVO::getId)
                    .collect(Collectors.toList());

            // 批量查询标签信息 - 这里可以增加缓存逻辑
            List<TagSimpleVO> allTags = articleTagService.listTagSimpleVoByArticleIds(articleIds);

            // 按文章ID分组标签
            Map<Long, List<TagSimpleVO>> tagsByArticleId = allTags.stream().collect(Collectors.groupingBy(TagSimpleVO::getArticleId));

            // 为每个文章设置标签列表
            articles.forEach(article -> {
                List<TagSimpleVO> tags = tagsByArticleId.getOrDefault(article.getId(), Collections.emptyList());
                tags.forEach(tag -> tag.setArticleId(null));
                article.setTags(tags);
            });

            log.debug("成功填充{}篇文章的标签信息，共{}个标签", articles.size(), allTags.size());
        } catch (Exception e) {
            log.error("填充文章标签信息失败", e);
            // 标签填充失败不应该影响主要数据的返回
            articles.forEach(article -> article.setTags(Collections.emptyList()));
        }
    }

}
