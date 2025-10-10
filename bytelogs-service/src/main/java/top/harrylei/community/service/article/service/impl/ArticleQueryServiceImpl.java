package top.harrylei.community.service.article.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.harrylei.community.api.enums.article.ArticlePublishStatusEnum;
import top.harrylei.community.api.enums.article.PublishedFlagEnum;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.model.article.dto.ArticleDTO;
import top.harrylei.community.api.model.article.req.ArticleQueryParam;
import top.harrylei.community.api.model.article.vo.ArticleVO;
import top.harrylei.community.api.model.article.dto.CategorySimpleDTO;
import top.harrylei.community.api.model.article.dto.TagSimpleDTO;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.PageUtils;
import top.harrylei.community.service.article.converted.ArticleStructMapper;
import top.harrylei.community.service.article.repository.dao.ArticleDAO;
import top.harrylei.community.service.article.repository.dao.ArticleDetailDAO;
import top.harrylei.community.service.article.repository.entity.ArticleDO;
import top.harrylei.community.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.community.service.article.service.ArticleQueryService;
import top.harrylei.community.service.article.service.ArticleTagService;
import top.harrylei.community.service.article.service.CategoryService;
import top.harrylei.community.service.article.service.TagService;

import java.util.List;
import java.util.Objects;

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
    private final TagService tagService;
    private final CategoryService categoryService;

    @Override
    public PageVO<ArticleVO> pageQuery(ArticleQueryParam queryParam) {
        // 处理查询权限
        processQueryPermissions(queryParam);

        // 创建MyBatis-Plus分页对象
        IPage<ArticleVO> page = PageUtils.of(queryParam);

        // 第一步：分页查询文章基础信息
        IPage<ArticleVO> articlePage = articleDAO.pageArticleVO(queryParam, page);

        // 第二步：批量填充标签和分类信息
        if (!CollectionUtils.isEmpty(articlePage.getRecords())) {
            fillArticleTagsAndCategories(articlePage.getRecords());
        }

        // 构建分页结果
        return PageUtils.from(articlePage);
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
                queryParam.setStatus(1); // PUBLISHED
                queryParam.setDeleted(0); // NOT_DELETED
            }
            return;
        }

        // 3. 公开查询
        if (!isAdmin) {
            // 普通用户只能看已发布且未删除的文章
            queryParam.setStatus(1); // PUBLISHED
            queryParam.setDeleted(0); // NOT_DELETED
        }
    }

    /**
     * 构建基础Article
     *
     * @param articleId 文章ID
     * @return 文章VO
     */
    @Override
    public ArticleDTO getPublishedArticle(Long articleId) {
        return getArticle(articleId, false);
    }

    @Override
    public ArticleDTO getLatestArticle(Long articleId) {
        return getArticle(articleId, true);
    }

    /**
     * 获取文章的通用方法
     */
    private ArticleDTO getArticle(Long articleId, boolean isLatestVersion) {
        ArticleDO article = articleDAO.getById(articleId);
        if (article == null) {
            ResultCode.ARTICLE_NOT_EXISTS.throwException();
        }

        // 根据参数选择版本
        ArticleDetailDO detail = isLatestVersion ? getLatestVersion(articleId) : getPublishedVersion(articleId);

        if (isLatestVersion) {
            // 最新版本只能作者或管理员查看
            Long currentUserId = ReqInfoContext.getContext().getUserId();
            boolean isAuthor = Objects.equals(article.getUserId(), currentUserId);
            boolean isAdmin = ReqInfoContext.getContext().isAdmin();
            if (PublishedFlagEnum.NO.equals(detail.getPublished()) && !isAuthor && !isAdmin) {
                ResultCode.OPERATION_NOT_ALLOWED.throwException();
            }
        }

        // 构建DTO
        ArticleDTO articleDTO = articleStructMapper.buildArticleDTO(article, detail);
        
        // 填充标签ID信息
        List<Long> tagIds = articleTagService.listTagIdsByArticleId(articleId);
        articleDTO.setTagIds(tagIds);
        
        // 填充SimpleDTO对象
        fillArticleDTOTagsAndCategory(articleDTO);
        
        return articleDTO;
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
     * 填充ArticleDTO的标签和分类信息
     */
    private void fillArticleDTOTagsAndCategory(ArticleDTO articleDTO) {
        // 填充标签信息
        if (articleDTO.getTagIds() != null && !articleDTO.getTagIds().isEmpty()) {
            List<TagSimpleDTO> tags = tagService.listSimpleTagsByIds(articleDTO.getTagIds());
            articleDTO.setTags(tags);
        } else {
            articleDTO.setTags(List.of());
        }
        
        // 填充分类信息
        if (articleDTO.getCategoryId() != null) {
            CategorySimpleDTO category = categoryService.getSimpleCategoryById(articleDTO.getCategoryId());
            articleDTO.setCategory(category);
        }
    }

    /**
     * 批量填充文章标签和分类信息
     */
    private void fillArticleTagsAndCategories(List<ArticleVO> articles) {
        if (CollectionUtils.isEmpty(articles)) {
            return;
        }

        // 为每个文章填充标签和分类信息
        for (ArticleVO article : articles) {
            // 填充标签信息
            List<Long> tagIds = articleTagService.listTagIdsByArticleId(article.getId());
            if (tagIds != null && !tagIds.isEmpty()) {
                List<TagSimpleDTO> tags = tagService.listSimpleTagsByIds(tagIds);
                article.setTags(tags);
            } else {
                article.setTags(List.of());
            }
            
            // 填充分类信息
            if (article.getCategoryId() != null) {
                CategorySimpleDTO category = categoryService.getSimpleCategoryById(article.getCategoryId());
                article.setCategory(category);
            }
        }
    }

}