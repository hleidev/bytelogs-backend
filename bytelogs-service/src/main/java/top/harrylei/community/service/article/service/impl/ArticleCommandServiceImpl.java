package top.harrylei.community.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.community.api.enums.article.*;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.notify.NotifyTypeEnum;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.enums.user.OperateTypeEnum;
import top.harrylei.community.api.event.NotificationEvent;
import top.harrylei.community.api.model.article.dto.ArticleDTO;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.KafkaEventPublisher;
import top.harrylei.community.service.article.converted.ArticleStructMapper;
import top.harrylei.community.service.article.repository.dao.ArticleDAO;
import top.harrylei.community.service.article.repository.dao.ArticleDetailDAO;
import top.harrylei.community.service.article.repository.entity.ArticleDO;
import top.harrylei.community.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.community.service.article.service.ArticleCommandService;
import top.harrylei.community.service.article.service.ArticleTagService;
import top.harrylei.community.service.user.service.UserFollowService;
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
    private final UserFootService userFootService;
    private final UserFollowService userFollowService;
    private final KafkaEventPublisher kafkaEventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveArticle(ArticleDTO articleDTO) {
        // 1. 处理审核逻辑
        ArticlePublishStatusEnum status = changeStatus(articleDTO.getStatus());

        // 2. 创建文章主记录
        ArticleDO articleDO = articleStructMapper.toDO(articleDTO);
        articleDO.setVersionCount(1);

        Long articleId = articleDAO.insertArticle(articleDO);

        // 3. 创建第一个版本
        saveArticleDetail(articleId, articleDTO, status);

        // 4. 处理标签
        if (articleDTO.getTagIds() != null && !articleDTO.getTagIds().isEmpty()) {
            articleTagService.saveBatch(articleId, articleDTO.getTagIds());
        }

        // 5. 如果是发布状态，发送文章发布通知给关注者
        if (ArticlePublishStatusEnum.PUBLISHED.equals(status)) {
            publishArticleNotificationEvent(articleId, articleDO.getUserId());
        }

        log.info("新建文章成功 articleId={} title={}", articleId, articleDTO.getTitle());
        return articleId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleDTO updateArticle(ArticleDTO articleDTO) {
        Long articleId = articleDTO.getId();
        if (articleId == null) {
            ResultCode.INVALID_PARAMETER.throwException();
        }

        // 1. 权限校验并获取原文章
        ArticleDO articleDO = getArticleBasicInfo(articleId);
        checkArticlePermission(articleDO.getUserId());

        // 2. 处理审核逻辑，确定最终状态
        ArticlePublishStatusEnum status = changeStatus(articleDTO.getStatus());

        // 3. 创建新版本
        Integer newVersion = articleDO.getVersionCount() + 1;
        ArticleDetailDO newDetail = createNewVersion(articleId, articleDTO, newVersion, status);

        // 4. 更新文章版本计数
        articleDO.setVersionCount(newVersion);
        articleDAO.updateById(articleDO);

        // 5. 更新标签
        List<Long> tagIds = articleDTO.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            articleTagService.updateTags(articleId, tagIds);
        }

        // 6. 返回最新文章信息
        ArticleDTO article = articleStructMapper.buildArticleDTO(articleDO, newDetail);
        log.info("文章内容更新成功 articleId={} status={}", articleId, status);
        return article;
    }

    @Override
    public void deleteArticle(Long articleId) {
        ArticleDO article = getArticleBasicInfo(articleId);
        checkArticlePermission(article.getUserId());

        if (checkAndUpdateDeleted(article, DeleteStatusEnum.DELETED)) {
            log.info("删除文章成功 articleId={} operatorId={}", articleId, ReqInfoContext.getContext().getUserId());
        } else {
            log.info("文章已删除，无需重复删除 articleId={}", articleId);
        }
    }

    @Override
    public void restoreArticle(Long articleId) {
        ArticleDO article = getDeletedArticle(articleId);
        checkArticlePermission(article.getUserId());

        if (checkAndUpdateDeleted(article, DeleteStatusEnum.NOT_DELETED)) {
            log.info("恢复文章成功 articleId={} operatorId={}", articleId, ReqInfoContext.getContext().getUserId());
        } else {
            log.info("文章未删除，无需恢复 articleId={}", articleId);
        }
    }

    @Override
    public void publishArticle(Long articleId) {
        updateArticleStatus(articleId, ArticlePublishStatusEnum.PUBLISHED);
    }

    @Override
    public void unpublishArticle(Long articleId) {
        updateArticleStatus(articleId, ArticlePublishStatusEnum.DRAFT);
    }

    @Override
    public void updateArticleStatus(Long articleId, ArticlePublishStatusEnum status) {
        ArticleDO article = getArticleBasicInfo(articleId);
        checkArticlePermission(article.getUserId());
        ArticleDetailDO detail = ArticlePublishStatusEnum.PUBLISHED.equals(status) ?
                articleDetailDAO.getLatestVersion(articleId) :
                articleDetailDAO.getPublishedVersion(articleId);

        if (detail == null) {
            ResultCode.ARTICLE_NOT_EXISTS.throwException();
        }

        // 处理状态变更逻辑
        ArticlePublishStatusEnum finalStatus = changeStatus(status);
        if (Objects.equals(finalStatus, detail.getStatus())) {
            log.warn("文章状态已是目标状态，无需更新 articleId={} status={}", articleId, finalStatus);
            return;
        }
        detail.setStatus(finalStatus);

        handlePublishStatus(detail, articleId, finalStatus);

        articleDetailDAO.updateById(detail);
        log.info("文章状态更新成功 articleId={} status={}", articleId, finalStatus);
    }

    @Override
    public void actionArticle(Long userId, Long articleId, OperateTypeEnum type) {
        // 获取文章验证存在性
        ArticleDO article = getArticleBasicInfo(articleId);
        userFootService.actionArticle(userId, type, article.getUserId(), articleId);
        log.info("文章操作成功 articleId={} userId={} type={}", articleId, userId, type);
    }

    @Override
    public ArticleDTO rollbackToVersion(Long articleId, Integer version) {
        ArticleDO article = getArticleBasicInfo(articleId);
        checkArticlePermission(article.getUserId());

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

        ArticleDTO result = articleStructMapper.buildArticleDTO(article, newDetail);
        log.info("文章版本回滚成功 articleId={} fromVersion={} toVersion={}", articleId, version, newVersion);
        return result;
    }

    private void saveArticleDetail(Long articleId, ArticleDTO articleDTO, ArticlePublishStatusEnum status) {
        ArticleDetailDO detail = articleStructMapper.toDetailDO(articleDTO);
        detail.setArticleId(articleId);
        detail.setVersion(1);
        detail.setLatest(LatestFlagEnum.YES);
        detail.setStatus(status);

        // 注意：saveArticleDetail是第一个版本，不需要清除旧发布标记
        if (ArticlePublishStatusEnum.PUBLISHED.equals(status)) {
            detail.setPublished(PublishedFlagEnum.YES);
            detail.setPublishTime(LocalDateTime.now());
        }

        articleDetailDAO.save(detail);
    }

    private ArticleDetailDO createNewVersion(Long articleId, ArticleDTO articleDTO, Integer version, ArticlePublishStatusEnum status) {
        ArticleDetailDO newDetail = articleStructMapper.toDetailDO(articleDTO);
        newDetail.setArticleId(articleId);
        newDetail.setVersion(version);
        newDetail.setLatest(LatestFlagEnum.YES);
        newDetail.setStatus(status);

        // 处理发布逻辑
        handlePublishStatus(newDetail, articleId, status);

        // 清除旧的最新版本标记并保存新版本
        articleDetailDAO.clearLatestFlag(articleId);
        articleDetailDAO.save(newDetail);

        return newDetail;
    }

    /**
     * 检查文章操作权限
     */
    private void checkArticlePermission(Long articleAuthorId) {
        Long currentUserId = ReqInfoContext.getContext().getUserId();
        boolean isAuthor = Objects.equals(articleAuthorId, currentUserId);
        boolean isAdmin = ReqInfoContext.getContext().isAdmin();

        if (currentUserId == null) {
            ResultCode.AUTHENTICATION_FAILED.throwException();
        }
        if (!isAuthor && !isAdmin) {
            ResultCode.FORBIDDEN.throwException();
        }
    }

    /**
     * 获取已删除的文章（仅用于恢复操作）
     */
    private ArticleDO getDeletedArticle(Long articleId) {
        ArticleDO article = articleDAO.getById(articleId);
        if (article == null || !DeleteStatusEnum.DELETED.equals(article.getDeleted())) {
            ResultCode.ARTICLE_NOT_EXISTS.throwException();
        }
        return article;
    }

    /**
     * 获取文章基础信息
     */
    private ArticleDO getArticleBasicInfo(Long articleId) {
        ArticleDO article = articleDAO.getById(articleId);
        if (article == null) {
            ResultCode.ARTICLE_NOT_EXISTS.throwException();
        }
        return article;
    }

    /**
     * 处理文章发布状态设置
     */
    private void handlePublishStatus(ArticleDetailDO detail, Long articleId, ArticlePublishStatusEnum status) {
        if (ArticlePublishStatusEnum.PUBLISHED.equals(status)) {
            articleDetailDAO.clearPublishedFlag(articleId);
            detail.setPublished(PublishedFlagEnum.YES);
            detail.setPublishTime(LocalDateTime.now());
        } else {
            detail.setPublished(PublishedFlagEnum.NO);
            detail.setPublishTime(null);
        }
    }

    private boolean checkAndUpdateDeleted(ArticleDO article, DeleteStatusEnum targetDeleted) {
        DeleteStatusEnum currentDeleted = article.getDeleted();
        if (Objects.equals(currentDeleted, targetDeleted)) {
            return false;
        }

        // 更新文章删除状态
        articleDAO.updateDeleted(article.getId(), targetDeleted);
        // 同步更新文章详情删除状态
        articleDetailDAO.updateDeleted(article.getId(), targetDeleted);
        return true;
    }

    private ArticlePublishStatusEnum changeStatus(ArticlePublishStatusEnum status) {
        // 获取当前用户信息
        boolean isAdmin = ReqInfoContext.getContext().isAdmin();

        switch (status) {
            case PUBLISHED -> {
                // 只有管理员可以直接发布，普通用户需要审核
                return isAdmin ? ArticlePublishStatusEnum.PUBLISHED : ArticlePublishStatusEnum.REVIEW;
            }
            case REJECTED, REVIEW -> {
                // 已驳回、待审核状态保持原状
                return status;
            }
            default -> {
                // 其他状态一律视为草稿
                return ArticlePublishStatusEnum.DRAFT;
            }
        }
    }

    private void publishArticleNotificationEvent(Long articleId, Long authorUserId) {
        // 获取所有关注者
        List<Long> followerIds = userFollowService.listFollowerIds(authorUserId);
        for (Long followerId : followerIds) {
            try {
                NotificationEvent event = NotificationEvent.builder()
                        .operateUserId(authorUserId)
                        .targetUserId(followerId)
                        .relatedId(articleId)
                        .notifyType(NotifyTypeEnum.ARTICLE_PUBLISH)
                        .contentType(ContentTypeEnum.ARTICLE)
                        .source("article-service")
                        .build();
                kafkaEventPublisher.publishNotificationEvent(event);
            } catch (Exception e) {
                log.warn("发送文章发布通知失败 articleId={} followerId={}", articleId, followerId, e);
            }
        }
    }

    @Override
    public void updateArticleTopping(Long articleId, ToppingStatusEnum toppingStat) {
        ArticleDO article = getArticleBasicInfo(articleId);
        checkArticlePermission(article.getUserId());

        articleDAO.updateTopping(articleId, toppingStat);
        log.info("文章置顶状态更新成功 articleId={} toppingStat={}", articleId, toppingStat);
    }

    @Override
    public void updateArticleCream(Long articleId, CreamStatusEnum creamStat) {
        ArticleDO article = getArticleBasicInfo(articleId);
        checkArticlePermission(article.getUserId());

        articleDAO.updateCream(articleId, creamStat);
        log.info("文章加精状态更新成功 articleId={} creamStat={}", articleId, creamStat);
    }

    @Override
    public void updateArticleOfficial(Long articleId, OfficialStatusEnum officialStat) {
        ArticleDO article = getArticleBasicInfo(articleId);
        checkArticlePermission(article.getUserId());

        articleDAO.updateOfficial(articleId, officialStat);
        log.info("文章官方状态更新成功 articleId={} officialStat={}", articleId, officialStat);
    }
}