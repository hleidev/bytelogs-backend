package top.harrylei.community.service.article.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.article.ArticlePublishStatusEnum;
import top.harrylei.community.api.model.article.req.ArticleQueryParam;
import top.harrylei.community.api.model.article.vo.ArticleDetailVO;
import top.harrylei.community.api.model.article.vo.ArticleVO;
import top.harrylei.community.api.model.article.vo.TagSimpleVO;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.api.model.statistics.StatisticsVO;
import top.harrylei.community.api.model.user.dto.ArticleFootCountDTO;
import top.harrylei.community.api.model.user.dto.UserInfoDTO;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.PageUtils;
import top.harrylei.community.service.article.converted.ArticleStructMapper;
import top.harrylei.community.service.article.repository.dao.ArticleDAO;
import top.harrylei.community.service.article.repository.dao.ArticleDetailDAO;
import top.harrylei.community.service.article.repository.entity.ArticleDO;
import top.harrylei.community.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.community.service.article.service.ArticleQueryService;
import top.harrylei.community.service.article.service.ArticleTagService;
import top.harrylei.community.service.statistics.service.ArticleStatisticsService;
import top.harrylei.community.service.user.converted.UserStructMapper;
import top.harrylei.community.service.user.service.UserFootService;
import top.harrylei.community.service.user.service.cache.UserCacheService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文章查询服务实现类
 * 负责文章的各种查询操作
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleQueryServiceImpl implements ArticleQueryService {

    private final ArticleDAO articleDAO;
    private final ArticleDetailDAO articleDetailDAO;
    private final ArticleStructMapper articleStructMapper;
    private final ArticleTagService articleTagService;
    private final UserStructMapper userStructMapper;
    private final UserCacheService userCacheService;
    private final UserFootService userFootService;
    private final ArticleStatisticsService articleStatisticsService;

    @Override
    public ArticleDetailVO getArticleDetail(Long articleId) {
        // 构建基础ArticleVO
        ArticleVO articleVO = getArticleVO(articleId, false);

        // 详情接口特有的权限校验
        validatePublicViewPermission(articleVO);

        // 构建统计信息
        StatisticsVO statistics = getStatistics(articleId);

        // 记录阅读行为
        recordReadBehavior(articleId, articleVO.getUserId());

        // 获取作者信息
        UserInfoDTO author = userCacheService.getUserInfo(articleVO.getUserId());

        ArticleDetailVO result = new ArticleDetailVO();
        result.setArticle(articleVO);
        result.setAuthor(userStructMapper.toVO(author));
        result.setStatistics(statistics);
        return result;
    }

    @Override
    public PageVO<ArticleVO> pageQuery(ArticleQueryParam queryParam) {
        // 处理查询权限
        processQueryPermissions(queryParam);

        // 创建MyBatis-Plus分页对象
        IPage<ArticleVO> page = PageUtils.of(queryParam);

        // 第一步：分页查询文章基础信息
        IPage<ArticleVO> articlePage = articleDAO.pageArticleVO(queryParam, page);

        // 第二步：批量查询标签信息并填充到结果中
        fillArticleTags(articlePage.getRecords());

        // 构建分页结果
        return PageUtils.from(articlePage);
    }

    @Override
    public ArticleDO getArticleById(Long articleId) {
        ArticleDO article = articleDAO.getById(articleId);
        if (article == null) {
            ResultCode.ARTICLE_NOT_EXISTS.throwException();
        }
        return article;
    }

    /**
     * 获取文章发布版本
     */
    private ArticleDetailDO getPublishedVersion(Long articleId) {
        ArticleDetailDO publishedVersion = articleDetailDAO.getPublishedVersion(articleId);
        if (publishedVersion == null) {
            ResultCode.ARTICLE_NOT_EXISTS.throwException();
        }
        return publishedVersion;
    }

    /**
     * 校验公开访问权限
     */
    private void validatePublicViewPermission(ArticleVO articleVO) {
        // 只校验已发布且未删除的文章
        if (DeleteStatusEnum.DELETED.equals(articleVO.getDeleted())) {
            ResultCode.ARTICLE_NOT_EXISTS.throwException();
        }

        if (!ArticlePublishStatusEnum.PUBLISHED.equals(articleVO.getStatus())) {
            ResultCode.ARTICLE_NOT_EXISTS.throwException();
        }
    }

    /**
     * 构建统计信息
     */
    private StatisticsVO getStatistics(Long articleId) {
        Long readCount = articleStatisticsService.getReadCount(articleId);
        ArticleFootCountDTO footCount = userFootService.getArticleFootCount(articleId);

        return new StatisticsVO()
                .setReadCount(readCount)
                .setPraiseCount(footCount.getPraiseCount())
                .setCollectionCount(footCount.getCollectionCount());
    }

    /**
     * 记录阅读行为
     */
    private void recordReadBehavior(Long articleId, Long authorId) {
        // 所有用户都增加阅读计数
        articleStatisticsService.incrementReadCount(articleId);

        // 仅登录用户记录足迹
        if (ReqInfoContext.getContext().isLoggedIn()) {
            Long currentUserId = ReqInfoContext.getContext().getUserId();
            userFootService.recordRead(currentUserId, authorId, articleId);
        }
    }

    /**
     * 处理查询权限
     */
    private void processQueryPermissions(ArticleQueryParam queryParam) {
        boolean isAdmin = ReqInfoContext.getContext().isAdmin();
        boolean isLogin = ReqInfoContext.getContext().isLoggedIn();
        Long currentUserId = ReqInfoContext.getContext().getUserId();

        // 1. 我的文章查询
        if (Boolean.TRUE.equals(queryParam.getOnlyMine())) {
            if (!isLogin) {
                ResultCode.AUTHENTICATION_FAILED.throwException("查询我的文章需要先登录");
            }
            queryParam.setUserId(currentUserId);
            return;
        }

        // 2. 指定用户查询
        if (queryParam.getUserId() != null) {
            boolean isAuthor = Objects.equals(queryParam.getUserId(), currentUserId);
            if (!isAuthor && !isAdmin) {
                // 查看他人文章，只能看已发布的
                queryParam.setStatus(ArticlePublishStatusEnum.PUBLISHED);
                queryParam.setDeleted(DeleteStatusEnum.NOT_DELETED);
            }
            return;
        }

        // 3. 公开查询
        if (!isAdmin) {
            // 普通用户只能看已发布且未删除的文章
            queryParam.setStatus(ArticlePublishStatusEnum.PUBLISHED);
            queryParam.setDeleted(DeleteStatusEnum.NOT_DELETED);
        }
    }

    /**
     * 构建基础ArticleVO
     *
     * @param articleId        文章ID
     * @param useLatestVersion 是否使用最新版本
     * @return 文章VO
     */
    @Override
    public ArticleVO getArticleVO(Long articleId, boolean useLatestVersion) {
        ArticleDO article = getArticleById(articleId);

        // 根据参数选择版本
        ArticleDetailDO detail = useLatestVersion
                ? getLatestVersion(articleId)
                : getPublishedVersion(articleId);

        // 构建VO和填充标签
        ArticleVO articleVO = articleStructMapper.buildArticleVO(article, detail);
        fillArticleTags(List.of(articleVO));

        return articleVO;
    }

    /**
     * 获取文章最新版本
     */
    private ArticleDetailDO getLatestVersion(Long articleId) {
        ArticleDetailDO latestVersion = articleDetailDAO.getLatestVersion(articleId);
        if (latestVersion == null) {
            ResultCode.ARTICLE_NOT_EXISTS.throwException();
        }
        return latestVersion;
    }

    /**
     * 批量填充文章标签信息
     */
    private void fillArticleTags(List<ArticleVO> articles) {
        if (CollectionUtils.isEmpty(articles)) {
            return;
        }

        List<Long> articleIds = articles.stream()
                .map(ArticleVO::getId)
                .toList();

        // 一次查询所有标签
        Map<Long, List<TagSimpleVO>> tagsMap = articleTagService
                .listTagSimpleVoByArticleIds(articleIds)
                .stream()
                .collect(Collectors.groupingBy(TagSimpleVO::getArticleId));

        // 填充到各个文章
        articles.forEach(article -> {
            List<TagSimpleVO> tags = tagsMap.getOrDefault(article.getId(), List.of());
            article.setTags(tags);
        });
    }
}