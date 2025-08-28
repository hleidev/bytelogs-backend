package top.harrylei.community.service.article.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.harrylei.community.api.enums.YesOrNoEnum;

import top.harrylei.community.api.model.article.dto.CategoryDTO;
import top.harrylei.community.api.model.article.req.CategoryReq;
import top.harrylei.community.api.model.article.vo.CategoryVO;
import top.harrylei.community.api.model.article.vo.CategorySimpleVO;
import top.harrylei.community.core.common.converter.EnumConverter;
import top.harrylei.community.service.article.repository.entity.CategoryDO;

/**
 * 分类对象转换映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring", uses = {EnumConverter.class})
public interface CategoryStructMapper {


    CategoryVO toVO(CategoryDTO categoryDTO);

    @Mapping(target = "deleted", source = "deleted", qualifiedByName = "CodeToYesOrNoEnum")
    CategoryDTO toDTO(CategoryDO categoryDO);


    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    CategoryDTO toDTO(CategoryReq categoryReq);

    @Mapping(target = "categoryId", source = "id")
    CategorySimpleVO toSimpleVO(CategoryDTO category);


    /**
     * YesOrNoEnum到Integer的自动映射
     */
    default Integer mapDeleted(YesOrNoEnum deleted) {
        return deleted != null ? deleted.getCode() : null;
    }

    /**
     * Integer到YesOrNoEnum的自动映射
     */
    default YesOrNoEnum mapDeleted(Integer code) {
        return YesOrNoEnum.fromCode(code);
    }
}
