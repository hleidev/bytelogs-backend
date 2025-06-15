package top.harrylei.forum.service.article.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import top.harrylei.forum.api.model.enums.article.ArticleSourceEnum;
import top.harrylei.forum.api.model.enums.article.ArticleTypeEnum;
import top.harrylei.forum.api.model.vo.article.dto.ArticleDTO;
import top.harrylei.forum.api.model.vo.article.req.ArticlePostReq;
import top.harrylei.forum.core.common.converter.EnumConverter;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;

/**
 * 文章对象转换映射器
 */
@Mapper(componentModel = "spring", uses = {EnumConverter.class})
public interface ArticleStructMapper{


    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "topping", ignore = true)
    @Mapping(target = "official", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "currentVersion", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "cream", ignore = true)
    ArticleDTO toDTO(ArticlePostReq articlePostReq);

    @Mapping(target = "articleType", source = "articleType", qualifiedByName = "ArticleTypeEnumToCode")
    @Mapping(target = "source", source = "source", qualifiedByName = "ArticleSourceEnumToCode")
    @Mapping(target = "official", source = "official", qualifiedByName = "YesOrNoEnumToCode")
    @Mapping(target = "topping", source = "topping", qualifiedByName = "YesOrNoEnumToCode")
    @Mapping(target = "cream", source = "cream", qualifiedByName = "YesOrNoEnumToCode")
    @Mapping(target = "deleted", source = "deleted", qualifiedByName = "YesOrNoEnumToCode")
    @Mapping(target = "status", source = "status", qualifiedByName = "PublishStatusEnumToCode")
    ArticleDO toDO(ArticleDTO articleDTO);

    @Named("ArticleTypeEnumToCode")
    default Integer articleTypeEnumToCode(ArticleTypeEnum articleTypeEnum) {
        return articleTypeEnum != null ? articleTypeEnum.getCode() : null;
    }

    @Named("ArticleSourceEnumToCode")
    default Integer articleSourceEnumToCode(ArticleSourceEnum articleSourceEnum) {
        return articleSourceEnum != null ? articleSourceEnum.getCode() : null;
    }
}
