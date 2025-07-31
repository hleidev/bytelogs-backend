package top.harrylei.forum.service.rank.service;

import top.harrylei.forum.api.event.UserActivityEvent;
import top.harrylei.forum.api.model.page.PageVO;
import top.harrylei.forum.api.model.rank.req.ActivityRankQueryParam;
import top.harrylei.forum.api.model.rank.vo.ActivityRankVO;

/**
 * 用户活跃度服务接口
 *
 * @author harry
 */
public interface UserActivityService {

    /**
     * 处理活跃度事件
     *
     * @param event 活跃度事件
     */
    void processActivityEvent(UserActivityEvent event);

    /**
     * 更新用户活跃度分数
     *
     * @param userId 用户ID
     * @param score  分数变化（正数为加分，负数为减分）
     */
    void updateUserScore(Long userId, Integer score);

    /**
     * 获取活跃度排行榜
     *
     * @param queryParam 查询参数
     * @return 排行榜分页数据
     */
    PageVO<ActivityRankVO> getActivityRankList(ActivityRankQueryParam queryParam);
}