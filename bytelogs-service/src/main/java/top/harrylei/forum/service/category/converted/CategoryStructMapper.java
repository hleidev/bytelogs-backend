package top.harrylei.forum.service.category.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import top.harrylei.forum.api.model.enums.CategoryStatusEnum;
import top.harrylei.forum.api.model.vo.article.CategoryReq;
import top.harrylei.forum.api.model.vo.article.dto.CategoryDTO;
import top.harrylei.forum.api.model.vo.article.vo.CategoryVO;
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

    @Mapping(target = "status", source = "status", qualifiedByName = "StatusEnumToCode")
    CategoryVO toVO(CategoryDTO categoryDTO);

    @Mapping(target = "status", source = "status", qualifiedByName = "codeToStatusEnum")
    CategoryDTO toDTO(CategoryDO categoryDO);

    @Named("StatusEnumToCode")
    default Integer StatusEnumToCode(CategoryStatusEnum statusEnum) {
        return statusEnum.getCode();
    }

    @Named("codeToStatusEnum")
    default CategoryStatusEnum codeToStatusEnum(Integer code) {
        return CategoryStatusEnum.fromCode(code);
    }
}
