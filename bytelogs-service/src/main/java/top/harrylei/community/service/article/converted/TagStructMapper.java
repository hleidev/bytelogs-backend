package top.harrylei.community.service.article.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import top.harrylei.community.api.enums.article.TagTypeEnum;
import top.harrylei.community.api.model.article.dto.TagDTO;
import top.harrylei.community.api.model.article.req.TagReq;
import top.harrylei.community.api.model.article.vo.TagVO;
import top.harrylei.community.api.model.article.vo.TagSimpleVO;
import top.harrylei.community.core.common.converter.EnumConverter;
import top.harrylei.community.service.article.repository.entity.TagDO;

/**
 * 标签对象转换映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring", uses = {EnumConverter.class})
public interface TagStructMapper {

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "creatorId", ignore = true)
    TagDTO toDTO(TagReq tagReq);

    @Mapping(target = "deleted", source = "deleted", qualifiedByName = "YesOrNoEnumToCode")
    @Mapping(target = "tagType", source = "tagType", qualifiedByName = "TagTypeEnumToCode")
    TagDO toDO(TagDTO tag);

    TagVO toVO(TagDTO tag);

    @Mapping(target = "deleted", source = "deleted", qualifiedByName = "CodeToYesOrNoEnum")
    @Mapping(target = "tagType", source = "tagType", qualifiedByName = "CodeToTagTypeEnum")
    TagDTO toDTO(TagDO tag);

    /**
     * TagTypeEnum到Integer的自动映射
     */
    default Integer mapTagType(TagTypeEnum tagType) {
        return tagType != null ? tagType.getCode() : null;
    }

    /**
     * Integer到TagTypeEnum的自动映射
     */
    default TagTypeEnum mapTagType(Integer code) {
        return TagTypeEnum.fromCode(code);
    }


    @Mapping(target = "articleId", ignore = true)
    @Mapping(target = "tagId", source = "id")
    @Mapping(target = "tagType", source = "tagType", qualifiedByName = "CodeToTagTypeEnum")
    TagSimpleVO toSimpleVO(TagDO tagDO);
}
