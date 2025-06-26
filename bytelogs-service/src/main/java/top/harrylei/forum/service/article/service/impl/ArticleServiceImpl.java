package top.harrylei.forum.service.article.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.article.ArticleStatusTypeEnum;
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
import top.harrylei.forum.core.util.NumUtil;
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
 * @author harry
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
        ExceptionUtil.requireValid(articleId, ErrorCodeEnum.PARAM_ERROR, "文章ID不能为空");

        // 权限校验并获取文章信息
        ArticleDO existingArticle = getArticleWithPermissionCheck(articleId);

        articleDTO.setUserId(existingArticle.getUserId());
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
        ArticleDO article = getArticleWithPermissionCheck(articleId);

        if (checkAndUpdateDeleted(article, YesOrNoEnum.YES)) {
            log.info("删除文章成功 articleId={} operatorId={}", articleId, ReqInfoContext.getContext().getUserId());
        } else {
            log.info("文章已删除，无需重复删除 articleId={}", articleId);
        }
    }

    /**
     * 恢复文章
     *
     * @param articleId 文章ID
     */
    @Override
    public void restoreArticle(Long articleId) {
        ArticleDO article = getArticleWithPermissionCheck(articleId, true);

        if (checkAndUpdateDeleted(article, YesOrNoEnum.NO)) {
            log.info("恢复文章成功 articleId={} operatorId={}", articleId, ReqInfoContext.getContext().getUserId());
        } else {
            log.info("文章未删除，无需恢复 articleId={}", articleId);
        }
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

        validateViewPermission(completeArticleVO.getUserId(), completeArticleVO.getDeleted(),
                               completeArticleVO.getStatus());

        UserInfoDetailDTO user = userCacheService.getUserInfo(completeArticleVO.getUserId());

        return new ArticleDetailVO().setArticle(completeArticleVO).setAuthor(userStructMapper.toVO(user));
    }

    /**
     * 更新文章状态
     *
     * @param articleId 文章ID
     * @param status    目标状态
     */
    @Override
    public void updateArticleStatus(Long articleId, PublishStatusEnum status) {
        // 获取文章
        ArticleDO article = getArticleById(articleId);

        // 状态变更业务逻辑处理
        PublishStatusEnum finalStatus = processStatusTransition(article, status);

        // 检查是否需要执行更新
        if (checkAndUpdateStatus(article, finalStatus)) {
            log.info("文章状态更新成功 articleId={} {} -> {} operatorId={}",
                     articleId, PublishStatusEnum.fromCode(article.getStatus()), finalStatus,
                     ReqInfoContext.getContext().getUserId());
        } else {
            log.info("文章状态未变更，无需更新 articleId={} status={}", articleId, finalStatus);
        }
    }

    /**
     * 分页查询文章
     *
     * @param queryParam 分页查询参数
     * @return 分页查询结果
     */
    @Override
    public PageVO<ArticleVO> pageQuery(ArticleQueryParam queryParam) {
        // 处理查询逻辑
        processQueryPermissions(queryParam);

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
     * 更新文章属性标识（置顶/加精/官方）
     *
     * @param articleId  文章ID
     * @param statusType 状态类型
     * @param status     是否启用
     */
    @Override
    public void updateArticleProperty(Long articleId, ArticleStatusTypeEnum statusType, YesOrNoEnum status) {
        // 获取文章并进行权限检查
        ArticleDO article = getArticleWithPermissionCheck(articleId);

        // 根据状态类型检查并更新对应字段
        boolean updated = switch (statusType) {
            case TOPPING -> checkAndUpdateTopping(article, status);
            case CREAM -> checkAndUpdateCream(article, status);
            case OFFICIAL -> checkAndUpdateOfficial(article, status);
        };

        if (updated) {
            log.info("更新文章{}属性成功 articleId={} enabled={} operatorId={}",
                     statusType.name(), articleId, status, ReqInfoContext.getContext().getUserId());
        } else {
            log.info("文章{}属性未变更，无需更新 articleId={} enabled={}", statusType.name(), articleId, status);
        }
    }

    /**
     * 处理查询权限逻辑
     *
     * @param queryParam 查询参数
     */
    private void processQueryPermissions(ArticleQueryParam queryParam) {
        // 1. 处理"只查询我的文章"逻辑
        processOnlyMineQuery(queryParam);

        // 2. 处理删除状态权限
        processDeletedStatusPermission(queryParam);

        // 3. 处理文章状态权限
        processStatusPermission(queryParam);

        // 4. 处理指定用户查询权限
        processUserQueryPermission(queryParam);

        log.debug("查询参数处理完成: userId={}, onlyMine={}, status={}, deleted={}, currentUser={}, isAdmin={}",
                  queryParam.getUserId(), queryParam.getOnlyMine(), queryParam.getStatus(),
                  queryParam.getDeleted(), getCurrentUserId(), isCurrentUserAdmin());
    }

    /**
     * 处理"只查询我的文章"逻辑
     */
    private void processOnlyMineQuery(ArticleQueryParam queryParam) {
        if (Boolean.TRUE.equals(queryParam.getOnlyMine())) {
            ExceptionUtil.errorIf(!isCurrentUserLoggedIn(), ErrorCodeEnum.UNAUTHORIZED, "请先登录");
            queryParam.setUserId(getCurrentUserId());
        }
    }

    /**
     * 处理删除状态权限
     */
    private void processDeletedStatusPermission(ArticleQueryParam queryParam) {
        if (queryParam.getDeleted() != null && YesOrNoEnum.YES.equals(queryParam.getDeleted())) {
            // 查看已删除文章需要作者权限或管理员权限
            if (queryParam.getUserId() != null) {
                validateOperatePermission(queryParam.getUserId());
            } else {
                // 查询所有已删除文章，只有管理员可以
                ExceptionUtil.errorIf(!isCurrentUserAdmin(), ErrorCodeEnum.FORBID_ERROR_MIXED, "无权限查看已删除文章");
            }
        } else if (queryParam.getDeleted() == null && !isCurrentUserAdmin()) {
            // 非管理员默认只查询未删除的文章
            queryParam.setDeleted(YesOrNoEnum.NO);
        }
    }

    /**
     * 处理文章状态权限
     */
    private void processStatusPermission(ArticleQueryParam queryParam) {
        if (queryParam.getStatus() != null) {
            // 查看非发布状态文章需要作者权限或管理员权限
            if (!PublishStatusEnum.PUBLISHED.equals(queryParam.getStatus())) {
                if (queryParam.getUserId() != null) {
                    validateOperatePermission(queryParam.getUserId());
                } else {
                    // 查询所有非发布状态文章，需要登录且是管理员
                    ExceptionUtil.errorIf(!isCurrentUserLoggedIn(), ErrorCodeEnum.UNAUTHORIZED, "请先登录");
                    ExceptionUtil.errorIf(!isCurrentUserAdmin(), ErrorCodeEnum.FORBID_ERROR_MIXED,
                                          "无权限查看该状态的文章");
                }
            }
        } else if (!canViewAllStatusArticles(queryParam.getUserId())) {
            // 未登录用户或查看他人文章时，只能看已发布的
            queryParam.setStatus(PublishStatusEnum.PUBLISHED);
        }
    }

    /**
     * 处理指定用户查询权限
     */
    private void processUserQueryPermission(ArticleQueryParam queryParam) {
        if (queryParam.getUserId() != null && !isCurrentUserAdmin() &&
                !canViewAllStatusArticles(queryParam.getUserId())) {
            // 非管理员查询他人文章时，限制为已发布且未删除
            queryParam.setStatus(PublishStatusEnum.PUBLISHED);
            queryParam.setDeleted(YesOrNoEnum.NO);
        }
    }

    /**
     * 判断当前用户是否可以查看所有状态的文章
     */
    private boolean canViewAllStatusArticles(Long targetUserId) {
        return isCurrentUserLoggedIn() &&
                (targetUserId == null || Objects.equals(targetUserId, getCurrentUserId()) || isCurrentUserAdmin());
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        return ReqInfoContext.getContext().getUserId();
    }

    /**
     * 检查当前用户是否已登录
     */
    private boolean isCurrentUserLoggedIn() {
        return ReqInfoContext.getContext().isLoggedIn();
    }

    /**
     * 检查当前用户是否为管理员
     */
    private boolean isCurrentUserAdmin() {
        return ReqInfoContext.getContext().isAdmin();
    }

    private ArticleDO getArticleWithPermissionCheck(Long articleId) {
        return getArticleWithPermissionCheck(articleId, false);
    }

    /**
     * 统一的文章获取方法
     *
     * @param articleId 文章ID
     * @return 文章DO对象
     */
    @Override
    public ArticleDO getArticleById(Long articleId) {
        if (NumUtil.nullOrZero(articleId)) {
            return null;
        }

        ArticleDO article = articleDAO.getById(articleId);
        ExceptionUtil.requireValid(article, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "articleId=" + articleId);
        return article;
    }

    /**
     * 获取文章并进行基础权限校验
     *
     * @param articleId      文章ID
     * @param includeDeleted 是否包含已删除的文章
     * @return 文章DO对象
     */
    private ArticleDO getArticleWithPermissionCheck(Long articleId, boolean includeDeleted) {
        ArticleDO article = getArticleById(articleId);

        // 如果不包含已删除文章，需要检查删除状态
        if (!includeDeleted && YesOrNoEnum.YES.getCode().equals(article.getDeleted())) {
            ExceptionUtil.error(ErrorCodeEnum.ARTICLE_NOT_EXISTS, "文章不存在: articleId=" + articleId);
        }

        // 基础权限校验：只有作者本人或管理员可以操作
        validateOperatePermission(article.getUserId());

        return article;
    }

    /**
     * 校验基础操作权限（作者或管理员）
     */
    private void validateOperatePermission(Long authorId) {
        Long operatorId = ReqInfoContext.getContext().getUserId();
        boolean isAdmin = ReqInfoContext.getContext().isAdmin();
        boolean isAuthor = Objects.equals(authorId, operatorId);

        ExceptionUtil.errorIf(!isAuthor && !isAdmin, ErrorCodeEnum.FORBID_ERROR_MIXED, "无权限操作此文章");
    }

    /**
     * 校验文章查看权限
     *
     * @param authorId 文章作者ID
     * @param deleted  删除状态
     * @param status   发布状态
     */
    private void validateViewPermission(Long authorId, YesOrNoEnum deleted, PublishStatusEnum status) {
        // 发布状态的文章，所有人都可以查看
        if (YesOrNoEnum.NO.equals(deleted) && PublishStatusEnum.PUBLISHED.equals(status)) {
            return;
        }

        // 非发布状态或已删除文章，需要权限校验
        validateOperatePermission(authorId);
    }

    /**
     * 检查并更新删除状态
     *
     * @param article       文章DO对象
     * @param targetDeleted 目标删除状态
     * @return 是否执行了更新操作
     */
    private boolean checkAndUpdateDeleted(ArticleDO article, YesOrNoEnum targetDeleted) {
        // 检查状态是否需要更新
        if (Objects.equals(article.getDeleted(), targetDeleted.getCode())) {
            return false;
        }

        // 执行状态更新
        updateArticleDeletedStatus(article.getId(), targetDeleted);
        return true;
    }

    /**
     * 检查并更新置顶状态
     *
     * @param article       文章DO对象
     * @param targetTopping 目标置顶状态
     * @return 是否执行了更新操作
     */
    private boolean checkAndUpdateTopping(ArticleDO article, YesOrNoEnum targetTopping) {
        // 检查状态是否需要更新
        if (Objects.equals(article.getTopping(), targetTopping.getCode())) {
            return false;
        }

        // 执行状态更新
        int updated = articleDAO.updateTopping(article.getId(), targetTopping.getCode());
        ExceptionUtil.errorIf(updated == 0, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "articleId=" + article.getId());
        return true;
    }

    /**
     * 检查并更新加精状态
     *
     * @param article     文章DO对象
     * @param targetCream 目标加精状态
     * @return 是否执行了更新操作
     */
    private boolean checkAndUpdateCream(ArticleDO article, YesOrNoEnum targetCream) {
        // 检查状态是否需要更新
        if (Objects.equals(article.getCream(), targetCream.getCode())) {
            return false;
        }

        // 执行状态更新
        int updated = articleDAO.updateCream(article.getId(), targetCream.getCode());
        ExceptionUtil.errorIf(updated == 0, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "articleId=" + article.getId());
        return true;
    }

    /**
     * 检查并更新官方状态
     *
     * @param article        文章DO对象
     * @param targetOfficial 目标官方状态
     * @return 是否执行了更新操作
     */
    private boolean checkAndUpdateOfficial(ArticleDO article, YesOrNoEnum targetOfficial) {
        // 检查状态是否需要更新
        if (Objects.equals(article.getOfficial(), targetOfficial.getCode())) {
            return false;
        }

        // 执行状态更新
        int updated = articleDAO.updateOfficial(article.getId(), targetOfficial.getCode());
        ExceptionUtil.errorIf(updated == 0, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "articleId=" + article.getId());
        return true;
    }

    /**
     * 处理文章状态转换业务逻辑
     *
     * @param article      文章DO对象
     * @param targetStatus 目标状态
     * @return 最终状态
     */
    private PublishStatusEnum processStatusTransition(ArticleDO article, PublishStatusEnum targetStatus) {
        // 状态转换权限校验
        validateOperatePermission(article.getUserId());
        validateStatusTransitionPermission(targetStatus);

        // 应用业务规则：非管理员发布需要审核
        if (needToReview(targetStatus)) {
            return PublishStatusEnum.REVIEW;
        }

        return targetStatus;
    }

    /**
     * 校验状态转换权限
     *
     * @param targetStatus 目标状态
     */
    private void validateStatusTransitionPermission(PublishStatusEnum targetStatus) {
        boolean isAdmin = ReqInfoContext.getContext().isAdmin();

        // 业务规则：审核状态只有管理员可以设置
        if ((targetStatus == PublishStatusEnum.PUBLISHED || targetStatus == PublishStatusEnum.REJECTED)
                && !isAdmin) {
            ExceptionUtil.error(ErrorCodeEnum.FORBID_ERROR_MIXED, "只有管理员可以审核文章");
        }
    }

    /**
     * 检查并更新文章状态
     *
     * @param article      文章DO对象
     * @param targetStatus 目标状态
     * @return 是否执行了更新操作
     */
    private boolean checkAndUpdateStatus(ArticleDO article, PublishStatusEnum targetStatus) {
        // 检查状态是否需要更新
        if (Objects.equals(article.getStatus(), targetStatus.getCode())) {
            return false;
        }

        // 执行状态更新
        int updated = articleDAO.updateStatus(article.getId(), targetStatus.getCode());
        ExceptionUtil.errorIf(updated == 0, ErrorCodeEnum.SYSTEM_ERROR, "更新文章状态失败");

        return true;
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
        ExceptionUtil.requireValid(status, ErrorCodeEnum.PARAM_MISSING, "状态码");
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
        PublishStatusEnum targetStatus = PublishStatusEnum.fromCode(article.getStatus());
        if (needToReview(targetStatus)) {
            article.setStatus(PublishStatusEnum.REVIEW.getCode());
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
        ExceptionUtil.requireValid(result, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "articleId=" + articleId);

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
                    .toList();

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
