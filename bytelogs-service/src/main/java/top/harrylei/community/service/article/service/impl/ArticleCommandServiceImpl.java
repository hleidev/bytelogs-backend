package top.harrylei.community.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.community.api.enums.ResultCode;
import top.harrylei.community.api.enums.notify.NotifyTypeEnum;
import top.harrylei.community.api.event.NotificationEvent;
import top.harrylei.community.api.enums.comment.ContentTypeEnum;
import top.harrylei.community.api.enums.user.OperateTypeEnum;
import top.harrylei.community.api.enums.YesOrNoEnum;
import top.harrylei.community.api.enums.article.ArticleStatusTypeEnum;
import top.harrylei.community.api.enums.article.PublishStatusEnum;
import top.harrylei.community.api.model.article.dto.ArticleDTO;
import top.harrylei.community.api.model.article.vo.ArticleVO;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.KafkaEventPublisher;
import top.harrylei.community.service.article.converted.ArticleStructMapper;
import top.harrylei.community.service.article.repository.dao.ArticleDAO;
import top.harrylei.community.service.article.repository.dao.ArticleDetailDAO;
import top.harrylei.community.service.article.repository.entity.ArticleDO;
import top.harrylei.community.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.community.service.article.service.ArticleCommandService;
import top.harrylei.community.service.article.service.ArticleQueryService;
import top.harrylei.community.service.article.service.ArticleTagService;
import top.harrylei.community.service.user.service.UserFootService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 文章命令服务实现类
 * 负责文章的保存、更新、删除、状态变更等写操作
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ArticleCommandServiceImpl implements ArticleCommandService {

    private final ArticleDAO articleDAO;
    private final ArticleDetailDAO articleDetailDAO;
    private final ArticleStructMapper articleStructMapper;
    private final ArticleTagService articleTagService;
    private final ArticleQueryService articleQueryService;
    private final UserFootService userFootService;
    private final KafkaEventPublisher kafkaEventPublisher;

    @Override
    public Long saveArticle(ArticleDTO articleDTO) {
        // 1. 处理审核逻辑
        PublishStatusEnum finalStatus = determinePublishStatus(articleDTO.getStatus());

        // 2. 创建文章主记录
        ArticleDO article = articleStructMapper.toDO(articleDTO);
        article.setVersionCount(1);

        Long articleId = articleDAO.insertArticle(article);

        // 3. 创建第一个版本
        saveArticleDetail(articleId, articleDTO, finalStatus);

        // 4. 处理标签
        saveArticleTags(articleId, articleDTO.getTagIds());

        // 5. 如果是发布状态，发送文章发布通知给关注者
        if (PublishStatusEnum.PUBLISHED.equals(finalStatus)) {
            publishArticleNotificationEvent(article);
        }

        log.info("新建文章成功 articleId={} title={}", articleId, articleDTO.getTitle());
        return articleId;
    }

    @Override
    public ArticleVO updateArticle(ArticleDTO articleDTO) {
        Long articleId = articleDTO.getId();
        if (articleId == null) {
            ResultCode.INVALID_PARAMETER.throwException();
        }

        // 1. 权限校验并获取原文章
        ArticleDO article = getArticleWithPermissionCheck(articleId);

        // 2. 处理审核逻辑，确定最终状态
        PublishStatusEnum finalStatus = determinePublishStatus(articleDTO.getStatus());

        // 3. 创建新版本
        Integer newVersion = article.getVersionCount() + 1;
        ArticleDetailDO newDetail = createNewVersion(articleId, articleDTO, newVersion, finalStatus);

        // 4. 更新文章版本计数
        article.setVersionCount(newVersion);
        articleDAO.updateById(article);

        // 5. 更新标签
        updateArticleTags(articleId, articleDTO.getTagIds());

        // 6. 返回最新文章信息
        ArticleVO result = articleStructMapper.buildArticleVO(article, newDetail);
        log.info("文章内容更新成功 articleId={} status={}", articleId, finalStatus);
        return result;
    }

    @Override
    public void deleteArticle(Long articleId) {
        ArticleDO article = getArticleWithPermissionCheck(articleId);

        if (checkAndUpdateDeleted(article, YesOrNoEnum.YES)) {
            log.info("删除文章成功 articleId={} operatorId={}", articleId, ReqInfoContext.getContext().getUserId());
        } else {
            log.info("文章已删除，无需重复删除 articleId={}", articleId);
        }
    }

    @Override
    public void restoreArticle(Long articleId) {
        ArticleDO article = getArticleWithPermissionCheck(articleId, true);

        if (checkAndUpdateDeleted(article, YesOrNoEnum.NO)) {
            log.info("恢复文章成功 articleId={} operatorId={}", articleId, ReqInfoContext.getContext().getUserId());
        } else {
            log.info("文章未删除，无需恢复 articleId={}", articleId);
        }
    }

    @Override
    public void publishArticle(Long articleId) {
        updateArticleStatus(articleId, PublishStatusEnum.PUBLISHED);
    }

    @Override
    public void unpublishArticle(Long articleId) {
        updateArticleStatus(articleId, PublishStatusEnum.DRAFT);
    }

    @Override
    public void updateArticleStatus(Long articleId, PublishStatusEnum status) {
        ArticleDO article = getArticleWithPermissionCheck(articleId);
        ArticleDetailDO targetDetail = PublishStatusEnum.PUBLISHED.equals(status) ?
                articleDetailDAO.getLatestVersion(articleId) :
                articleDetailDAO.getPublishedVersion(articleId);

        if (targetDetail == null) {
            ResultCode.ARTICLE_NOT_EXISTS.throwException();
        }

        // 处理状态变更逻辑
        PublishStatusEnum finalStatus = determinePublishStatus(status);
        if (Objects.equals(finalStatus.getCode(), targetDetail.getStatus())) {
            log.warn("文章状态已是目标状态，无需更新 articleId={} status={}", articleId, finalStatus);
            return;
        }
        targetDetail.setStatus(finalStatus.getCode());

        if (PublishStatusEnum.PUBLISHED.equals(finalStatus)) {
            // 清除旧的发布标记
            articleDetailDAO.clearPublishedFlag(articleId);
            targetDetail.setPublished(YesOrNoEnum.YES.getCode());
            targetDetail.setPublishTime(LocalDateTime.now());
        } else {
            targetDetail.setPublished(YesOrNoEnum.NO.getCode());
            targetDetail.setPublishTime(null);
        }

        articleDetailDAO.updateById(targetDetail);
        log.info("文章状态更新成功 articleId={} status={}", articleId, finalStatus);
    }

    @Override
    public void updateArticleProperty(Long articleId, ArticleStatusTypeEnum statusType, YesOrNoEnum status) {
        ArticleDO article = getArticleWithPermissionCheck(articleId);

        switch (statusType) {
            case TOPPING -> articleDAO.updateTopping(articleId, status.getCode());
            case CREAM -> articleDAO.updateCream(articleId, status.getCode());
            case OFFICIAL -> articleDAO.updateOfficial(articleId, status.getCode());
            default -> throw new IllegalArgumentException("不支持的状态类型: " + statusType);
        }

        log.info("文章属性更新成功 articleId={} statusType={} status={}", articleId, statusType, status);
    }

    @Override
    public void actionArticle(Long userId, Long articleId, OperateTypeEnum type) {
        // 获取文章验证存在性
        ArticleDO article = articleQueryService.getArticleById(articleId);
        userFootService.actionArticle(userId, type, article.getUserId(), articleId);
        log.info("文章操作成功 articleId={} userId={} type={}", articleId, userId, type);
    }

    @Override
    public ArticleVO rollbackToVersion(Long articleId, Integer version) {
        ArticleDO article = getArticleWithPermissionCheck(articleId);

        // 获取目标版本
        ArticleDetailDO targetVersion = articleDetailDAO.getByArticleIdAndVersion(articleId, version);
        if (targetVersion == null) {
            ResultCode.ARTICLE_NOT_EXISTS.throwException();
        }

        // 创建新版本（基于目标版本的内容）
        Integer newVersion = article.getVersionCount() + 1;
        ArticleDetailDO newDetail = articleStructMapper.copyForNewVersion(targetVersion);
        newDetail.setArticleId(articleId);
        newDetail.setVersion(newVersion);

        // 清除旧的最新版本标记
        articleDetailDAO.clearLatestFlag(articleId);
        articleDetailDAO.save(newDetail);

        // 更新版本计数
        article.setVersionCount(newVersion);
        articleDAO.updateById(article);

        ArticleVO result = articleStructMapper.buildArticleVO(article, newDetail);
        log.info("文章版本回滚成功 articleId={} fromVersion={} toVersion={}", articleId, version, newVersion);
        return result;
    }

    private void saveArticleDetail(Long articleId,
                                   ArticleDTO articleDTO,
                                   PublishStatusEnum status) {
        ArticleDetailDO detail = articleStructMapper.toDetailDO(articleDTO);
        detail.setArticleId(articleId);
        detail.setVersion(1);
        detail.setLatest(YesOrNoEnum.YES.getCode());
        detail.setStatus(status.getCode());

        if (PublishStatusEnum.PUBLISHED.equals(status)) {
            detail.setPublished(YesOrNoEnum.YES.getCode());
            detail.setPublishTime(LocalDateTime.now());
        }

        articleDetailDAO.save(detail);
    }

    private ArticleDetailDO createNewVersion(Long articleId,
                                             ArticleDTO articleDTO,
                                             Integer version,
                                             PublishStatusEnum status) {
        ArticleDetailDO newDetail = articleStructMapper.toDetailDO(articleDTO);
        newDetail.setArticleId(articleId);
        newDetail.setVersion(version);
        newDetail.setLatest(YesOrNoEnum.YES.getCode());
        newDetail.setStatus(status.getCode());

        // 处理发布逻辑
        if (PublishStatusEnum.PUBLISHED.equals(status)) {
            articleDetailDAO.clearPublishedFlag(articleId);
            newDetail.setPublished(YesOrNoEnum.YES.getCode());
            newDetail.setPublishTime(LocalDateTime.now());
        }

        // 清除旧的最新版本标记并保存新版本
        articleDetailDAO.clearLatestFlag(articleId);
        articleDetailDAO.save(newDetail);

        return newDetail;
    }

    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            articleTagService.saveBatch(articleId, tagIds);
        }
    }

    private void updateArticleTags(Long articleId, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            articleTagService.updateTags(articleId, tagIds);
        }
    }

    private ArticleDO getArticleWithPermissionCheck(Long articleId) {
        return getArticleWithPermissionCheck(articleId, false);
    }

    private ArticleDO getArticleWithPermissionCheck(Long articleId, boolean allowDeleted) {
        ArticleDO article = articleDAO.getById(articleId);
        if (article == null) {
            ResultCode.ARTICLE_NOT_EXISTS.throwException();
        }

        // 权限校验：只允许作者操作自己的文章
        Long currentUserId = ReqInfoContext.getContext().getUserId();
        boolean isLogin = ReqInfoContext.getContext().isLoggedIn();
        if (!isLogin) {
            ResultCode.AUTHENTICATION_FAILED.throwException();
        }
        if (!Objects.equals(article.getUserId(), currentUserId)) {
            ResultCode.FORBIDDEN.throwException();
        }

        // 删除状态校验
        if (!allowDeleted && YesOrNoEnum.YES.getCode().equals(article.getDeleted())) {
            ResultCode.ARTICLE_NOT_EXISTS.throwException();
        }

        return article;
    }

    private boolean checkAndUpdateDeleted(ArticleDO article, YesOrNoEnum targetDeleted) {
        Integer currentDeleted = article.getDeleted();
        if (Objects.equals(currentDeleted, targetDeleted.getCode())) {
            return false;
        }

        // 更新文章删除状态
        articleDAO.updateDeleted(article.getId(), targetDeleted.getCode());
        // 同步更新文章详情删除状态
        articleDetailDAO.updateDeleted(article.getId(), targetDeleted.getCode());
        return true;
    }

    private PublishStatusEnum determinePublishStatus(PublishStatusEnum requestStatus) {
        // 如果请求状态为空，默认为草稿
        if (requestStatus == null) {
            return PublishStatusEnum.DRAFT;
        }

        // 获取当前用户信息
        boolean isAdmin = ReqInfoContext.getContext().isAdmin();

        // 审核策略：
        // 1. 管理员可以直接发布，无需审核
        // 2. 普通用户发布文章需要进入审核流程
        // 3. 草稿和审核驳回状态保持不变
        switch (requestStatus) {
            case PUBLISHED -> {
                // 只有管理员可以直接发布，普通用户需要审核
                return isAdmin ? PublishStatusEnum.PUBLISHED : PublishStatusEnum.REVIEW;
            }
            case DRAFT, REJECTED, REVIEW -> {
                // 草稿、已驳回、待审核状态保持原状
                return requestStatus;
            }
            default -> {
                log.warn("未知的发布状态: {}, 默认设置为草稿", requestStatus);
                return PublishStatusEnum.DRAFT;
            }
        }
    }

    private void publishArticleNotificationEvent(ArticleDO article) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .operateUserId(article.getUserId())
                    .targetUserId(article.getUserId())
                    .relatedId(article.getId())
                    .notifyType(NotifyTypeEnum.ARTICLE_PUBLISH.getCode())
                    .contentType(ContentTypeEnum.ARTICLE.getCode())
                    .source("article-service")
                    .build();
            kafkaEventPublisher.publishNotificationEvent(event);
        } catch (Exception e) {
            log.warn("发送文章发布通知失败 articleId={}", article.getId(), e);
        }
    }

    @Override
    public ArticleVO getArticleDraft(Long articleId) {
        // 使用ArticleQueryService的公共方法getArticleVO
        ArticleVO articleVO = articleQueryService.getArticleVO(articleId, true);

        // 权限校验
        validateAuthorPermission(articleVO);

        return articleVO;
    }

    /**
     * 校验作者权限
     */
    private void validateAuthorPermission(ArticleVO articleVO) {
        Long currentUserId = ReqInfoContext.getContext().getUserId();

        if (!Objects.equals(articleVO.getUserId(), currentUserId)) {
            ResultCode.FORBIDDEN.throwException();
        }
    }
}