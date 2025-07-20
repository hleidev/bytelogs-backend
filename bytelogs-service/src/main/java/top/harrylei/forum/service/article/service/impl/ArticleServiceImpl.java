package top.harrylei.forum.service.article.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.enums.NotifyTypeEnum;
import top.harrylei.forum.api.model.enums.OperateTypeEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.comment.ContentTypeEnum;
import top.harrylei.forum.api.model.enums.article.ArticleStatusTypeEnum;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.vo.article.dto.ArticleDTO;
import top.harrylei.forum.api.model.vo.article.req.ArticleQueryParam;
import top.harrylei.forum.api.model.vo.article.vo.ArticleDetailVO;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.api.model.vo.article.vo.TagSimpleVO;
import top.harrylei.forum.api.model.vo.statistics.StatisticsVO;
import top.harrylei.forum.api.model.vo.page.PageHelper;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.core.util.KafkaEventPublisher;
import top.harrylei.forum.core.util.NumUtil;
import top.harrylei.forum.service.article.converted.ArticleStructMapper;
import top.harrylei.forum.service.article.repository.dao.ArticleDAO;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;
import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.forum.service.article.service.ArticleDetailService;
import top.harrylei.forum.service.article.service.ArticleService;
import top.harrylei.forum.service.article.service.ArticleTagService;
import top.harrylei.forum.service.statistics.service.ReadCountService;
import top.harrylei.forum.service.user.converted.UserStructMapper;
import top.harrylei.forum.service.user.service.UserFollowService;
import top.harrylei.forum.service.user.service.UserFootService;
import top.harrylei.forum.service.user.service.cache.UserCacheService;

import java.time.LocalDateTime;
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

    private final ArticleDAO articleDAO;
    private final ArticleStructMapper articleStructMapper;
    private final ArticleDetailService articleDetailService;
    private final ArticleTagService articleTagService;
    private final UserStructMapper userStructMapper;
    private final UserCacheService userCacheService;
    private final UserFootService userFootService;
    private final UserFollowService userFollowService;
    private final ReadCountService readCountService;
    private final KafkaEventPublisher kafkaEventPublisher;

    /**
     * 保存文章
     *
     * @param articleDTO 文章传输对象
     * @return 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveArticle(ArticleDTO articleDTO) {
        // 1. 处理审核逻辑
        PublishStatusEnum finalStatus = applyReviewPolicy(articleDTO.getStatus());

        // 2. 创建文章主记录
        ArticleDO article = articleStructMapper.toDO(articleDTO);
        article.setVersionCount(1);

        Long articleId = articleDAO.insertArticle(article);

        // 3. 创建第一个版本
        ArticleDetailDO detail = articleStructMapper.toDetailDO(articleDTO);
        detail.setArticleId(articleId);
        detail.setVersion(1);
        detail.setLatest(YesOrNoEnum.YES.getCode());
        detail.setStatus(finalStatus.getCode());

        // 4. 处理发布逻辑
        if (PublishStatusEnum.PUBLISHED.equals(finalStatus)) {
            detail.setPublished(YesOrNoEnum.YES.getCode());
            detail.setPublishTime(LocalDateTime.now());
        }

        articleDetailService.save(detail);

        // 5. 处理标签
        List<Long> tagIds = articleDTO.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            articleTagService.saveBatch(articleId, tagIds);
        }

        // 如果是发布状态，发送文章发布通知给关注者
        if (PublishStatusEnum.PUBLISHED.equals(finalStatus)) {
            publishArticleNotificationEvent(article);
        }

        log.info("新建文章成功 articleId={} title={}", articleId, articleDTO.getTitle());
        return articleId;
    }

    /**
     * 更新文章
     *
     * @param articleDTO 文章传输对象
     * @return 文章VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO updateArticle(ArticleDTO articleDTO) {
        Long articleId = articleDTO.getId();
        ExceptionUtil.requireValid(articleId, ErrorCodeEnum.PARAM_ERROR, "文章ID不能为空");

        // 1. 权限校验并获取原文章
        ArticleDO article = getArticleWithPermissionCheck(articleId);

        // 2. 处理审核逻辑，确定最终状态
        PublishStatusEnum finalStatus = applyReviewPolicy(articleDTO.getStatus());

        // 3. 创建新版本
        Integer newVersion = article.getVersionCount() + 1;
        ArticleDetailDO newDetail = articleStructMapper.toDetailDO(articleDTO);
        newDetail.setArticleId(articleId);
        newDetail.setVersion(newVersion);
        newDetail.setLatest(YesOrNoEnum.YES.getCode());
        newDetail.setStatus(finalStatus.getCode());

        // 4. 处理发布逻辑
        if (PublishStatusEnum.PUBLISHED.equals(finalStatus)) {
            // 清除旧的发布标记
            articleDetailService.clearPublishedFlag(articleId);
            newDetail.setPublished(YesOrNoEnum.YES.getCode());
            newDetail.setPublishTime(LocalDateTime.now());
        }

        // 5. 事务性更新
        articleDetailService.clearLatestFlag(articleId);
        articleDetailService.save(newDetail);
        article.setVersionCount(newVersion);
        articleDAO.updateById(article);

        // 6. 更新标签
        List<Long> tagIds = articleDTO.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            articleTagService.updateTags(articleId, tagIds);
        }

        // 7. 返回最新文章信息
        ArticleVO result = articleStructMapper.buildArticleVO(article, newDetail);
        log.info("文章内容更新成功 articleId={} status={}", articleId, finalStatus);
        return result;
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
        Long currentUserId = ReqInfoContext.getContext().getUserId();

        // 使用智能版本获取逻辑
        ArticleDetailDO detail = getSmartVersion(articleId, currentUserId);
        ArticleDO article = getArticleById(articleId);

        // 权限校验
        validateViewPermission(article.getUserId(), YesOrNoEnum.fromCode(article.getDeleted()),
                               PublishStatusEnum.fromCode(detail.getStatus()));

        // 构建完整的文章VO
        ArticleVO articleVO = articleStructMapper.buildArticleVO(article, detail);

        // 填充标签信息
        fillArticleTags(List.of(articleVO));

        // 构建统计信息
        Integer readCount = readCountService.getReadCount(articleId, ContentTypeEnum.ARTICLE);
        StatisticsVO statistics = new StatisticsVO().setReadCount(readCount);

        UserInfoDetailDTO user = userCacheService.getUserInfo(article.getUserId());

        // 记录阅读行为
        recordReadBehavior(articleId);

        return new ArticleDetailVO()
                .setArticle(articleVO)
                .setAuthor(userStructMapper.toVO(user))
                .setStatistics(statistics);
    }

    /**
     * 记录阅读行为
     *
     * @param articleId 文章ID
     */
    private void recordReadBehavior(Long articleId) {
        // 异步增加阅读量（所有用户）
        readCountService.incrementReadCount(articleId, ContentTypeEnum.ARTICLE);

        // 记录用户阅读行为（仅登录用户）
        if (ReqInfoContext.getContext().isLoggedIn()) {
            ArticleDO article = getArticleById(articleId);

            Long currentUserId = ReqInfoContext.getContext().getUserId();
            if (currentUserId != null) {
                Boolean success = userFootService.recordRead(currentUserId, article.getUserId(), articleId);
                if (success) {
                    log.info("文章阅读记录成功 articleId={} userId={}", articleId, currentUserId);
                }
            }
        }
    }

    /**
     * 发布文章
     *
     * @param articleId 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishArticle(Long articleId) {
        updateArticleStatus(articleId, PublishStatusEnum.PUBLISHED);
        log.info("文章发布成功 articleId={}", articleId);
    }

    /**
     * 撤销发布
     *
     * @param articleId 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublishArticle(Long articleId) {
        updateArticleStatus(articleId, PublishStatusEnum.DRAFT);
        log.info("文章撤销发布成功 articleId={}", articleId);
    }

    /**
     * 更新文章状态
     *
     * @param articleId 文章ID
     * @param status    目标状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticleStatus(Long articleId, PublishStatusEnum status) {
        // 获取文章并校验权限
        ArticleDO article = getArticleWithPermissionCheck(articleId);

        // 状态转换权限校验
        boolean isAdmin = ReqInfoContext.getContext().isAdmin();
        // 审核状态只有管理员可以设置
        if (status == PublishStatusEnum.REJECTED && !isAdmin) {
            ExceptionUtil.error(ErrorCodeEnum.FORBID_ERROR_MIXED, "只有管理员可以审核文章");
        }

        // 应用业务规则：非管理员发布需要审核
        PublishStatusEnum finalStatus = applyReviewPolicy(status);

        // 获取当前最新版本
        ArticleDetailDO latestDetail = articleDetailService.getLatestVersion(articleId);
        ExceptionUtil.requireValid(latestDetail, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "文章最新版本不存在");

        // 如果状态没有变化，直接返回
        if (PublishStatusEnum.fromCode(latestDetail.getStatus()).equals(finalStatus)) {
            log.info("文章状态无变化，跳过更新 articleId={} status={}", articleId, finalStatus);
            return;
        }

        // 直接更新最新版本的状态，不创建新版本
        latestDetail.setStatus(finalStatus.getCode());

        // 处理发布逻辑
        if (PublishStatusEnum.PUBLISHED.equals(finalStatus)) {
            // 清除旧的发布标记，设置当前版本为发布版本
            articleDetailService.clearPublishedFlag(articleId);
            latestDetail.setPublished(YesOrNoEnum.YES.getCode());
            latestDetail.setPublishTime(java.time.LocalDateTime.now());
        } else {
            // 非发布状态，只清除当前版本的发布标记，不影响其他版本
            latestDetail.setPublished(YesOrNoEnum.NO.getCode());
            latestDetail.setPublishTime(null);
        }

        // 更新最新版本
        boolean updated = articleDetailService.updateById(latestDetail);
        ExceptionUtil.errorIf(!updated, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "更新文章状态失败");

        // 如果是发布状态，发送文章发布通知给关注者
        if (PublishStatusEnum.PUBLISHED.equals(finalStatus)) {
            publishArticleNotificationEvent(article);
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

    /**
     * 应用审核策略，返回实际状态
     *
     * @param requestedStatus 用户请求的状态
     * @return 经过审核策略处理后的实际状态
     */
    private PublishStatusEnum applyReviewPolicy(PublishStatusEnum requestedStatus) {
        // 管理员可以直接设置任何状态
        if (ReqInfoContext.getContext().isAdmin()) {
            return requestedStatus;
        }

        // 普通用户发布需要审核（除非在白名单中）
        // TODO 添加用户白名单逻辑
        if (PublishStatusEnum.PUBLISHED.equals(requestedStatus)) {
            return PublishStatusEnum.REVIEW;
        }

        // 其他状态（草稿、待审核等）直接返回
        return requestedStatus;
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

    /**
     * 文章操作
     *
     * @param articleId 文章ID
     * @param type      操作类型
     */
    @Override
    public void actionArticle(Long articleId, OperateTypeEnum type) {
        ArticleDO article = getArticleById(articleId);

        Long currentUserId = ReqInfoContext.getContext().getUserId();

        Boolean success = userFootService.actionArticle(currentUserId,
                                                        type,
                                                        article.getUserId(),
                                                        articleId);
        ExceptionUtil.errorIf(!success, ErrorCodeEnum.FORBIDDEN_OPERATION, "操作失败，请稍后重试");

        log.info("文章{}操作成功 articleId={} type={}", type.getLabel(), articleId, type);
    }

    /**
     * 智能获取版本：作者看最新，读者看发布
     */
    public ArticleDetailDO getSmartVersion(Long articleId, Long currentUserId) {
        ArticleDO article = getArticleById(articleId);

        // 作者本人：返回最新版本
        if (Objects.equals(article.getUserId(), currentUserId)) {
            return articleDetailService.getLatestVersion(articleId);
        }

        // 其他人：返回发布版本
        ArticleDetailDO published = articleDetailService.getPublishedVersion(articleId);
        if (published == null) {
            ExceptionUtil.error(ErrorCodeEnum.ARTICLE_NOT_EXISTS, "文章未发布");
        }
        return published;
    }

    /**
     * 回滚到指定版本
     *
     * @param articleId 文章ID
     * @param version   目标版本号
     * @return 回滚后的文章VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO rollbackToVersion(Long articleId, Integer version) {
        // 1. 权限校验并获取文章
        ArticleDO article = getArticleWithPermissionCheck(articleId);

        // 2. 版本范围验证
        ExceptionUtil.errorIf(version > article.getVersionCount(),
                              ErrorCodeEnum.PARAM_VALIDATE_FAILED,
                              "目标版本不存在: version=" + version);

        // 3. 检查是否回滚到当前版本
        ArticleDetailDO currentLatest = articleDetailService.getLatestVersion(articleId);
        ExceptionUtil.requireValid(currentLatest, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "文章当前版本不存在");
        ExceptionUtil.errorIf(Objects.equals(currentLatest.getVersion(), version),
                              ErrorCodeEnum.PARAM_VALIDATE_FAILED,
                              "不能回滚到当前版本");

        // 4. 获取目标版本
        ArticleDetailDO targetDetail = articleDetailService.getByArticleIdAndVersion(articleId, version);
        ExceptionUtil.requireValid(targetDetail,
                                   ErrorCodeEnum.ARTICLE_NOT_EXISTS,
                                   "目标版本不存在: version=" + version);

        // 5. 创建回滚版本
        ArticleDetailDO rollbackDetail = createRollbackVersion(article, targetDetail);

        // 6. 更新版本计数
        article.setVersionCount(article.getVersionCount() + 1);
        articleDAO.updateById(article);

        // 7. 重新获取更新后的文章信息并返回
        article = articleDAO.getById(articleId);
        ArticleVO result = articleStructMapper.buildArticleVO(article, rollbackDetail);

        log.info("文章版本回滚成功 articleId={} targetVersion={} newVersion={} operatorId={}",
                 articleId,
                 version,
                 rollbackDetail.getVersion(),
                 ReqInfoContext.getContext().getUserId());

        return result;
    }

    /**
     * 创建回滚版本
     *
     * @param article      文章DO对象
     * @param sourceDetail 源版本详情
     * @return 新创建的回滚版本
     */
    private ArticleDetailDO createRollbackVersion(ArticleDO article, ArticleDetailDO sourceDetail) {
        // 复制内容字段，排除版本元数据
        ArticleDetailDO rollbackDetail = articleStructMapper.copyForRollback(sourceDetail);

        // 设置新版本的元数据
        rollbackDetail.setVersion(article.getVersionCount() + 1);
        rollbackDetail.setLatest(YesOrNoEnum.YES.getCode());
        rollbackDetail.setPublished(YesOrNoEnum.NO.getCode());
        rollbackDetail.setStatus(PublishStatusEnum.DRAFT.getCode());
        rollbackDetail.setPublishTime(null);

        // 先保存新版本，再清除旧的最新标记
        articleDetailService.save(rollbackDetail);
        articleDetailService.clearLatestFlag(article.getId());

        // 确保新版本的latest标记正确设置
        rollbackDetail.setLatest(YesOrNoEnum.YES.getCode());
        articleDetailService.updateById(rollbackDetail);

        return rollbackDetail;
    }

    /**
     * 发布文章通知事件给关注者
     *
     * @param article 文章信息
     */
    private void publishArticleNotificationEvent(ArticleDO article) {
        try {
            Long authorId = article.getUserId();
            Long articleId = article.getId();

            List<Long> followerIds = userFollowService.getFollowerIds(authorId);
            if (followerIds.isEmpty()) {
                log.debug("作者无关注者，跳过文章发布通知: authorId={}, articleId={}", authorId, articleId);
                return;
            }

            for (Long followerId : followerIds) {
                kafkaEventPublisher.publishUserBehaviorEvent(authorId,
                                                             followerId,
                                                             articleId,
                                                             ContentTypeEnum.ARTICLE,
                                                             NotifyTypeEnum.ARTICLE_PUBLISH);
            }

            log.debug("发布文章通知事件成功: authorId={}, articleId={}, followerCount={}",
                      authorId, articleId, followerIds.size());

        } catch (Exception e) {
            log.error("发布文章通知事件失败: articleId={}", article.getId(), e);
        }
    }

}
