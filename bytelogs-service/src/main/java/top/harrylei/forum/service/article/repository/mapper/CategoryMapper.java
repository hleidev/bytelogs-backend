package top.harrylei.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.harrylei.forum.api.model.vo.article.vo.CategorySimpleVO;
import top.harrylei.forum.service.article.repository.entity.CategoryDO;

public interface CategoryMapper extends BaseMapper<CategoryDO> {

    CategorySimpleVO getSimpleById(Long id);
}
