package top.harrylei.community.service.article.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.harrylei.community.api.model.article.dto.ArticleDTO;
import top.harrylei.community.api.model.article.req.ArticleSaveReq;
import top.harrylei.community.api.model.article.req.ArticleUpdateReq;
import top.harrylei.community.api.model.article.vo.ArticleVO;
import top.harrylei.community.api.model.article.vo.ArticleVersionVO;
import top.harrylei.community.service.article.repository.entity.ArticleDO;
import top.harrylei.community.service.article.repository.entity.ArticleDetailDO;


/**
 * 文章对象转换映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring")
public interface ArticleStructMapper {


    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "versionCount", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "topping", ignore = true)
    @Mapping(target = "official", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "cream", ignore = true)
    ArticleDTO toDTO(ArticleSaveReq articleSaveReq);

    @Mapping(target = "versionCount", ignore = true)
    ArticleDO toDO(ArticleDTO articleDTO);

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "tagIds", ignore = true)
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "shortTitle", ignore = true)
    @Mapping(target = "picture", ignore = true)
    @Mapping(target = "summary", ignore = true)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "sourceUrl", ignore = true)
    @Mapping(target = "status", ignore = true)
    ArticleDTO toDTO(ArticleDO articleDO);

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "versionCount", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "topping", ignore = true)
    @Mapping(target = "official", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "cream", ignore = true)
    ArticleDTO toDTO(ArticleUpdateReq articleUpdateReq);

    ArticleVO toVO(ArticleDTO article);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "articleId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "latest", ignore = true)
    @Mapping(target = "published", ignore = true)
    @Mapping(target = "publishTime", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    ArticleDetailDO toDetailDO(ArticleDTO articleDTO);

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "tagIds", ignore = true)
    @Mapping(target = "id", source = "article.id")
    @Mapping(target = "userId", source = "article.userId")
    @Mapping(target = "deleted", source = "article.deleted")
    @Mapping(target = "createTime", source = "article.createTime")
    @Mapping(target = "updateTime", source = "article.updateTime")
    @Mapping(target = "title", source = "detail.title")
    @Mapping(target = "shortTitle", source = "detail.shortTitle")
    @Mapping(target = "picture", source = "detail.picture")
    @Mapping(target = "summary", source = "detail.summary")
    @Mapping(target = "sourceUrl", source = "detail.sourceUrl")
    @Mapping(target = "content", source = "detail.content")
    @Mapping(target = "categoryId", source = "detail.categoryId")
    ArticleDTO buildArticleDTO(ArticleDO article, ArticleDetailDO detail);

    ArticleVersionVO toVersionVO(ArticleDetailDO detail);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "articleId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "latest", constant = "YES")
    @Mapping(target = "published", constant = "NO")
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "publishTime", ignore = true)
    @Mapping(target = "deleted", constant = "NOT_DELETED")
    ArticleDetailDO copyForNewVersion(ArticleDetailDO source);
}
