package top.harrylei.forum.service.rank.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.forum.api.event.ActivityEvent;
import top.harrylei.forum.service.rank.service.UserActivityService;

/**
 * 用户活跃度服务实现
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processActivityEvent(ActivityEvent event) {
        try {
            // 获取分数变化
            Integer scoreChange = event.getActionType().getScore();

            log.debug("处理活跃度事件: userId={}, action={}, score={}",
                      event.getUserId(),
                      event.getActionType().getLabel(),
                      scoreChange);

            // 更新用户分数
            updateUserScore(event.getUserId(), scoreChange);

            // TODO: 记录活跃度详情到数据库
            // TODO: 更新Redis排行榜

            log.info("活跃度事件处理完成: userId={}, action={}, score={}",
                     event.getUserId(),
                     event.getActionType().getLabel(),
                     scoreChange);

        } catch (Exception e) {
            log.error("处理活跃度事件失败: userId={}, action={}", event.getUserId(), event.getActionType(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserScore(Long userId, Integer score) {
        if (score == 0) {
            log.debug("分数变化为0，跳过处理: userId={}", userId);
            return;
        }

        log.debug("更新用户分数: userId={}, scoreChange={}", userId, score);

        // TODO: 实现分数更新逻辑
        // 1. 更新用户总分数
        // 2. 更新Redis排行榜
        // 3. 记录分数变化明细

        log.info("用户分数更新完成: userId={}, scoreChange={}", userId, score);
    }
}