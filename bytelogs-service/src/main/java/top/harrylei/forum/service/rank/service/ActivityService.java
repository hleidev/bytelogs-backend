package top.harrylei.forum.service.rank.service;

import top.harrylei.forum.api.event.ActivityRankEvent;

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
    void processActivityEvent(ActivityRankEvent event);

    /**
     * 更新用户活跃度分数
     *
     * @param userId 用户ID
     * @param score  分数变化（正数为加分，负数为减分）
     */
    void updateUserScore(Long userId, Integer score);
}