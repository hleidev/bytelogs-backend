package top.harrylei.forum.service.article.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import top.harrylei.forum.api.model.enums.article.ArticleSourceEnum;
import top.harrylei.forum.api.model.enums.article.ArticleTypeEnum;
import top.harrylei.forum.api.model.vo.article.dto.ArticleDTO;
import top.harrylei.forum.api.model.vo.article.req.ArticlePostReq;
import top.harrylei.forum.api.model.vo.article.req.ArticleUpdateReq;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.core.common.converter.EnumConverter;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;
import top.harrylei.forum.service.article.repository.entity.ArticleDetailDO;


/**
 * 文章对象转换映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring", uses = {EnumConverter.class})
public interface ArticleStructMapper {

    @Named("ArticleTypeEnumToCode")
    default Integer articleTypeEnumToCode(ArticleTypeEnum articleTypeEnum) {
        return articleTypeEnum != null ? articleTypeEnum.getCode() : null;
    }

    @Named("ArticleSourceEnumToCode")
    default Integer articleSourceEnumToCode(ArticleSourceEnum articleSourceEnum) {
        return articleSourceEnum != null ? articleSourceEnum.getCode() : null;
    }

    @Named("CodeToArticleTypeEnum")
    default ArticleTypeEnum codeToArticleTypeEnum(Integer code) {
        return code != null ? ArticleTypeEnum.fromCode(code) : null;
    }

    @Named("CodeToArticleSourceEnum")
    default ArticleSourceEnum codeToArticleSourceEnum(Integer code) {
        return code != null ? ArticleSourceEnum.fromCode(code) : null;
    }

    @Mapping(target = "versionCount", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "topping", ignore = true)
    @Mapping(target = "official", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "cream", ignore = true)
    ArticleDTO toDTO(ArticlePostReq articlePostReq);

    @Mapping(target = "articleType", source = "articleType", qualifiedByName = "ArticleTypeEnumToCode")
    @Mapping(target = "official", source = "official", qualifiedByName = "YesOrNoEnumToCode")
    @Mapping(target = "topping", source = "topping", qualifiedByName = "YesOrNoEnumToCode")
    @Mapping(target = "cream", source = "cream", qualifiedByName = "YesOrNoEnumToCode")
    @Mapping(target = "deleted", source = "deleted", qualifiedByName = "YesOrNoEnumToCode")
    @Mapping(target = "versionCount", ignore = true)
    ArticleDO toDO(ArticleDTO articleDTO);

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
    @Mapping(target = "articleType", source = "articleType", qualifiedByName = "CodeToArticleTypeEnum")
    @Mapping(target = "official", source = "official", qualifiedByName = "CodeToYesOrNoEnum")
    @Mapping(target = "topping", source = "topping", qualifiedByName = "CodeToYesOrNoEnum")
    @Mapping(target = "cream", source = "cream", qualifiedByName = "CodeToYesOrNoEnum")
    @Mapping(target = "deleted", source = "deleted", qualifiedByName = "CodeToYesOrNoEnum")
    ArticleDTO toDTO(ArticleDO articleDO);

    @Mapping(target = "versionCount", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "topping", ignore = true)
    @Mapping(target = "official", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "cream", ignore = true)
    ArticleDTO toDTO(ArticleUpdateReq articleUpdateReq);

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "category", ignore = true)
    ArticleVO toVO(ArticleDTO article);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "articleId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "latest", ignore = true)
    @Mapping(target = "published", ignore = true)
    @Mapping(target = "publishTime", ignore = true)
    @Mapping(target = "source", source = "source", qualifiedByName = "ArticleSourceEnumToCode")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    ArticleDetailDO toDetailDO(ArticleDTO articleDTO);

    @Mapping(target = "id", source = "article.id")
    @Mapping(target = "userId", source = "article.userId")
    @Mapping(target = "articleType", source = "article.articleType", qualifiedByName = "CodeToArticleTypeEnum")
    @Mapping(target = "official", source = "article.official", qualifiedByName = "CodeToYesOrNoEnum")
    @Mapping(target = "topping", source = "article.topping", qualifiedByName = "CodeToYesOrNoEnum")
    @Mapping(target = "cream", source = "article.cream", qualifiedByName = "CodeToYesOrNoEnum")
    @Mapping(target = "deleted", source = "article.deleted", qualifiedByName = "CodeToYesOrNoEnum")
    @Mapping(target = "createTime", source = "article.createTime")
    @Mapping(target = "updateTime", source = "article.updateTime")
    @Mapping(target = "title", source = "detail.title")
    @Mapping(target = "shortTitle", source = "detail.shortTitle")
    @Mapping(target = "picture", source = "detail.picture")
    @Mapping(target = "summary", source = "detail.summary")
    @Mapping(target = "source", source = "detail.source", qualifiedByName = "CodeToArticleSourceEnum")
    @Mapping(target = "sourceUrl", source = "detail.sourceUrl")
    @Mapping(target = "status", source = "detail.status", qualifiedByName = "CodeToPublishStatusEnum")
    @Mapping(target = "content", source = "detail.content")
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "category", ignore = true)
    ArticleVO buildArticleVO(ArticleDO article, ArticleDetailDO detail);
}
