package top.harrylei.forum.service.rank.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.forum.api.enums.rank.ActivityActionEnum;
import top.harrylei.forum.api.event.UserActivityEvent;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;
import top.harrylei.forum.core.util.RedisUtil;
import top.harrylei.forum.service.rank.service.UserActivityService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 用户活跃度服务实现
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService {

    private final RedisUtil redisUtil;

    private static final Integer DAILY_SCORE_LIMIT = 100;
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private String getDayKey() {
        LocalDate today = LocalDate.now();
        String dayStr = today.format(DAY_FORMATTER);
        return RedisKeyConstants.getUserActivityDailyRankKey(dayStr);
    }

    private String getMonthKey() {
        LocalDate today = LocalDate.now();
        String monthStr = today.format(MONTH_FORMATTER);
        return RedisKeyConstants.getUserActivityMonthlyRankKey(monthStr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processActivityEvent(UserActivityEvent event) {
        try {
            ActivityActionEnum actionEnum = ActivityActionEnum.fromCode(event.getActionType());
            if (actionEnum == null) {
                log.error("无效的活跃度行为类型: actionType={}", event.getActionType());
                return;
            }
            Integer scoreChange = actionEnum.getScore();
            updateUserScore(event.getUserId(), scoreChange);
        } catch (Exception e) {
            log.error("处理活跃度事件失败: userId={}, action={}", event.getUserId(), event.getActionType(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserScore(Long userId, Integer score) {
        if (score == 0) {
            return;
        }

        Integer currentScore = checkDailyScoreLimit(userId, score);
        if (currentScore == 0) {
            return;
        }

        String userIdStr = userId.toString();

        // 更新总排行榜
        redisUtil.zIncrBy(RedisKeyConstants.getUserActivityTotalRankKey(), userIdStr, currentScore);

        // 更新日排行榜（1天过期）
        String dailyRankKey = getDayKey();
        redisUtil.zIncrBy(dailyRankKey, userIdStr, currentScore);
        redisUtil.expire(dailyRankKey, Duration.ofDays(1));

        // 更新月排行榜（30天过期）
        String monthlyRankKey = getMonthKey();
        redisUtil.zIncrBy(monthlyRankKey, userIdStr, currentScore);
        redisUtil.expire(monthlyRankKey, Duration.ofDays(30));
    }

    private Integer checkDailyScoreLimit(Long userId, Integer score) {
        if (score <= 0) {
            return score;
        }

        LocalDate now = LocalDate.now();
        String dayStr = now.format(DAY_FORMATTER);

        String limitKey = RedisKeyConstants.getUserActivityDailyLimitKey(userId, dayStr);
        Long currentScore = redisUtil.get(limitKey, Long.class);

        if (currentScore == null) {
            // 只在key不存在时设置过期时间
            redisUtil.set(limitKey, score.longValue(), Duration.ofDays(1));
            return score;
        }

        if (currentScore + score > DAILY_SCORE_LIMIT) {
            score = DAILY_SCORE_LIMIT - currentScore.intValue();
            if (score <= 0) {
                return 0;
            }
        }

        // 只更新数值，不重置过期时间
        redisUtil.set(limitKey, currentScore + score);
        return score;
    }
}