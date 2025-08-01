package top.harrylei.forum.service.article.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import top.harrylei.forum.api.enums.article.TagTypeEnum;
import top.harrylei.forum.api.model.article.dto.TagDTO;
import top.harrylei.forum.api.model.article.req.TagReq;
import top.harrylei.forum.api.model.article.vo.TagVO;
import top.harrylei.forum.api.model.article.vo.TagSimpleVO;
import top.harrylei.forum.core.common.converter.EnumConverter;
import top.harrylei.forum.service.article.repository.entity.TagDO;

/**
 * 标签对象转换映射器
 */
@Mapper(componentModel = "spring", uses = {EnumConverter.class})
public interface TagStructMapper {

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    TagDTO toDTO(TagReq tagReq);

    @Mapping(target = "status", source = "status", qualifiedByName = "PublishStatusEnumToCode")
    @Mapping(target = "deleted", source = "deleted", qualifiedByName = "YesOrNoEnumToCode")
    @Mapping(target = "tagType", source = "tagType", qualifiedByName = "TagTypeEnumToCode")
    TagDO toDO(TagDTO tag);

    TagVO toVO(TagDTO tag);

    @Mapping(target = "status", source = "status", qualifiedByName = "CodeToPublishStatusEnum")
    @Mapping(target = "deleted", source = "deleted", qualifiedByName = "CodeToYesOrNoEnum")
    @Mapping(target = "tagType", source = "tagType", qualifiedByName = "CodeToTagTypeEnum")
    TagDTO toDTO(TagDO tag);

    @Named("TagTypeEnumToCode")
    default Integer TagTypeEnumToCode(TagTypeEnum tagTypeEnum) {
        return tagTypeEnum == null ? null : tagTypeEnum.getCode();
    }

    @Named("TagTypeEnumToLabel")
    default String TagTypeEnumToLabel(TagTypeEnum tagTypeEnum) {
        return tagTypeEnum == null ? null : tagTypeEnum.getLabel();
    }

    @Named("CodeToTagTypeEnum")
    default TagTypeEnum CodeToTagTypeEnum(Integer code) {
        return code == null ? null : TagTypeEnum.fromCode(code);
    }

    @Mapping(target = "status", source = "status", qualifiedByName = "PublishStatusEnumToCode")
    @Mapping(target = "tagType", source = "tagType", qualifiedByName = "TagTypeEnumToCode")
    @Mapping(target = "deleted", ignore = true)
    void updateDOFromDTO(TagDTO tagDTO, @MappingTarget TagDO tagDO);

    @Mapping(target = "articleId", ignore = true)
    @Mapping(target = "tagId", source = "id")
    @Mapping(target = "tagType", source = "tagType", qualifiedByName = "CodeToTagTypeEnum")
    TagSimpleVO toSimpleVO(TagDO tagDO);
}
