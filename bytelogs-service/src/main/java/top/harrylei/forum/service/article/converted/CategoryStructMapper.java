package top.harrylei.forum.service.article.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import top.harrylei.forum.api.model.vo.article.dto.CategoryDTO;
import top.harrylei.forum.api.model.vo.article.req.CategoryReq;
import top.harrylei.forum.api.model.vo.article.vo.CategoryVO;
import top.harrylei.forum.api.model.vo.article.vo.CategorySimpleVO;
import top.harrylei.forum.core.common.converter.EnumConverter;
import top.harrylei.forum.service.article.repository.entity.CategoryDO;

/**
 * 分类对象转换映射器
 */
@Mapper(componentModel = "spring", uses = {EnumConverter.class})
public interface CategoryStructMapper {

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "PublishStatusEnumToCode")
    void updateDOFromDTO(CategoryDTO categoryDTO, @MappingTarget CategoryDO category);

    @Mapping(target = "status", source = "status", qualifiedByName = "PublishStatusEnumToCode")
    CategoryVO toVO(CategoryDTO categoryDTO);

    @Mapping(target = "status", source = "status", qualifiedByName = "CodeToPublishStatusEnum")
    CategoryDTO toDTO(CategoryDO categoryDO);


    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    CategoryDTO toDTO(CategoryReq categoryReq);

    @Mapping(target = "categoryId", source = "id")
    CategorySimpleVO toSimpleVO(CategoryDTO category);
}
