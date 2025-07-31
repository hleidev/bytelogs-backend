package top.harrylei.forum.service.rank.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.harrylei.forum.service.rank.repository.entity.ActivityRankDO;

/**
 * 活跃度排行榜Mapper接口
 *
 * @author harry
 */
@Mapper
public interface ActivityRankMapper extends BaseMapper<ActivityRankDO> {

}