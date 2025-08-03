package top.harrylei.forum.service.rank.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.harrylei.forum.service.rank.service.ActivityService;

/**
 * 活跃度排行榜定时任务
 *
 * @author harry
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityRankTask {

    private final ActivityService activityService;

    /**
     * 每日凌晨4点备份排行榜数据
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void backupRankingData() {
        log.info("开始定时备份排行榜数据");
        try {
            activityService.backupAllRankingData();
            log.info("定时备份排行榜数据完成");
        } catch (Exception e) {
            log.error("定时备份排行榜数据失败", e);
        }
    }
}