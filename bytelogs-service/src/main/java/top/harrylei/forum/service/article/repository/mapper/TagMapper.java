package top.harrylei.forum.service.article.repository.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import top.harrylei.forum.api.model.vo.article.vo.TagSimpleVO;
import top.harrylei.forum.service.article.repository.entity.TagDO;

public interface TagMapper extends BaseMapper<TagDO> {

    List<TagSimpleVO> listSimpleTagsByIds(List<Long> ids);
}
