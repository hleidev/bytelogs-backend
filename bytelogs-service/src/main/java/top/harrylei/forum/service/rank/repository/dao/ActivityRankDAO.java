package top.harrylei.forum.service.rank.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.service.rank.repository.entity.ActivityRankDO;
import top.harrylei.forum.service.rank.repository.mapper.ActivityRankMapper;

/**
 * 活跃度排行榜DAO
 *
 * @author harry
 */
@Repository
public class ActivityRankDAO extends ServiceImpl<ActivityRankMapper, ActivityRankDO> {

    /**
     * 物理删除指定类型和期间的排行榜数据
     */
    public boolean removeByTypeAndPeriod(Integer rankType, String rankPeriod) {
        return lambdaUpdate()
                .eq(ActivityRankDO::getRankType, rankType)
                .eq(ActivityRankDO::getRankPeriod, rankPeriod)
                .remove();
    }

}