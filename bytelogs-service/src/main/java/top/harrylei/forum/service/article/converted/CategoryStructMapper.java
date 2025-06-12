package top.harrylei.forum.service.article.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import top.harrylei.forum.api.model.vo.article.dto.CategoryDTO;
import top.harrylei.forum.api.model.vo.article.req.CategoryReq;
import top.harrylei.forum.api.model.vo.article.vo.CategoryDetailVO;
import top.harrylei.forum.api.model.vo.article.vo.CategoryVO;
import top.harrylei.forum.core.common.converter.EnumConverter;
import top.harrylei.forum.service.article.repository.entity.CategoryDO;

/**
 * 分类对象转换映射器
 */
@Mapper(componentModel = "spring", uses = {EnumConverter.class})
public interface CategoryStructMapper {

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "PublishStatusEnumToCode")
    void updateDOFromReq(CategoryReq req, @MappingTarget CategoryDO category);

    @Mapping(target = "categoryId", source = "id")
    @Mapping(target = "status", source = "status", qualifiedByName = "PublishStatusEnumToCode")
    CategoryDetailVO toAdminVO(CategoryDTO categoryDTO);

    @Mapping(target = "status", source = "status", qualifiedByName = "CodeToPublishStatusEnum")
    CategoryDTO toDTO(CategoryDO categoryDO);

    @Mapping(target = "categoryId", source = "id")
    CategoryVO toVO(CategoryDTO category);
}
