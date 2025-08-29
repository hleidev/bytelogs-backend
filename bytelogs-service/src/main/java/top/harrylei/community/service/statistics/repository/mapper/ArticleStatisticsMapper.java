package top.harrylei.community.service.statistics.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.harrylei.community.service.statistics.repository.entity.ReadCountDO;

/**
 * 内容访问计数Mapper接口
 *
 * @author harry
 */
@Mapper
public interface ReadCountMapper extends BaseMapper<ReadCountDO> {

}