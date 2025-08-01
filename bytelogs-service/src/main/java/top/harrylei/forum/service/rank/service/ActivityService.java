package top.harrylei.forum.service.rank.service;

import top.harrylei.forum.api.enums.rank.ActivityRankTypeEnum;
import top.harrylei.forum.api.event.ActivityRankEvent;
import top.harrylei.forum.api.model.rank.dto.ActivityRankDTO;
import top.harrylei.forum.api.model.rank.vo.ActivityRankVO;
import top.harrylei.forum.api.model.rank.vo.ActivityStatsVO;

import java.util.List;

/**
 * 用户活跃度服务接口
 *
 * @author harry
 */
public interface ActivityService {

    /**
     * 处理活跃度事件
     *
     * @param event 活跃度事件
     */
    void handleActivityEvent(ActivityRankEvent event);

    /**
     * 更新用户活跃度分数
     *
     * @param userId 用户ID
     * @param score  分数变化（正数为加分，负数为减分）
     */
    void updateUserScore(Long userId, Integer score);

    List<ActivityRankDTO> listRank(ActivityRankTypeEnum rankType);

    ActivityRankVO getUserRank(Long userId, ActivityRankTypeEnum rankType);

    ActivityStatsVO getUserStats(Long userId);
}