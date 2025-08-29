package top.harrylei.community.service.article.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.harrylei.community.api.model.article.dto.TagDTO;
import top.harrylei.community.api.model.article.req.TagReq;
import top.harrylei.community.api.model.article.vo.TagSimpleVO;
import top.harrylei.community.api.model.article.vo.TagVO;
import top.harrylei.community.service.article.repository.entity.TagDO;

/**
 * 标签对象转换映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring")
public interface TagStructMapper {

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "creatorId", ignore = true)
    TagDTO toDTO(TagReq tagReq);

    TagDO toDO(TagDTO tag);

    TagVO toVO(TagDTO tag);

    TagDTO toDTO(TagDO tag);

    @Mapping(target = "articleId", ignore = true)
    @Mapping(target = "tagId", source = "id")
    TagSimpleVO toSimpleVO(TagDO tagDO);
}
