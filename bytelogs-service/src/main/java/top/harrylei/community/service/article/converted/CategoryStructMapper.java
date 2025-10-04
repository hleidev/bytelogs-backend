package top.harrylei.community.service.article.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.harrylei.community.api.model.article.dto.CategoryDTO;
import top.harrylei.community.api.model.article.req.CategoryReq;
import top.harrylei.community.api.model.article.dto.CategorySimpleDTO;
import top.harrylei.community.api.model.article.vo.CategoryVO;
import top.harrylei.community.service.article.repository.entity.CategoryDO;

/**
 * 分类对象转换映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring")
public interface CategoryStructMapper {


    CategoryVO toVO(CategoryDTO categoryDTO);

    CategoryDTO toDTO(CategoryDO categoryDO);


    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    CategoryDTO toDTO(CategoryReq categoryReq);

    @Mapping(target = "categoryId", source = "id")
    CategorySimpleDTO toSimpleVO(CategoryDO category);

    @Mapping(target = "categoryId", source = "id")
    CategorySimpleDTO toSimpleVO(CategoryDTO category);
}
