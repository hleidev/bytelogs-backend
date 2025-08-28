package top.harrylei.community.service.rank.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.community.service.rank.repository.entity.ActivityRankDO;
import top.harrylei.community.service.rank.repository.mapper.ActivityRankMapper;

import java.util.List;

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

    /**
     * 查询指定类型和期间的历史排行榜数据
     */
    public List<ActivityRankDO> listRanking(Integer rankType, String rankPeriod) {
        return lambdaQuery()
                .eq(ActivityRankDO::getRankType, rankType)
                .eq(ActivityRankDO::getRankPeriod, rankPeriod)
                .orderByDesc(ActivityRankDO::getScore)
                .last("LIMIT 100")
                .list();
    }

    /**
     * 查询指定用户在某期间某类型排行榜中的排名和积分
     */
    public ActivityRankDO getUserHistoryRank(Long userId, Integer rankType, String rankPeriod) {
        return lambdaQuery()
                .eq(ActivityRankDO::getUserId, userId)
                .eq(ActivityRankDO::getRankType, rankType)
                .eq(ActivityRankDO::getRankPeriod, rankPeriod)
                .one();
    }

}