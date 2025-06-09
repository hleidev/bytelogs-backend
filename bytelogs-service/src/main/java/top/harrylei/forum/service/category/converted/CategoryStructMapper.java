package top.harrylei.forum.service.category.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import top.harrylei.forum.api.model.vo.article.CategoryReq;
import top.harrylei.forum.service.category.repository.entity.CategoryDO;

/**
 * 分类对象转换映射器
 */
@Mapper(componentModel = "spring")
public interface CategoryStructMapper {

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    void updateDOFromReq(CategoryReq req, @MappingTarget CategoryDO category);
}
