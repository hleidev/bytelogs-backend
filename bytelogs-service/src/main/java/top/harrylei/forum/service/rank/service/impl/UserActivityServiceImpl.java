package top.harrylei.forum.service.rank.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.forum.api.enums.rank.ActivityActionEnum;
import top.harrylei.forum.api.event.UserActivityEvent;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;
import top.harrylei.forum.core.util.NumUtil;
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

    private static final String SCORE_TOTAL_FIELD = "score_total";
    private static final Integer DAILY_SCORE_LIMIT = 100;
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private String getDayKey() {
        String dayStr = getDateStr(DAY_FORMATTER);
        return RedisKeyConstants.getUserActivityDailyRankKey(dayStr);
    }

    private String getMonthKey() {
        String monthStr = getDateStr(MONTH_FORMATTER);
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
            Integer actualScore = scoreIdempotency(event, scoreChange);

            if (actualScore > 0) {
                updateUserScore(event.getUserId(), actualScore);
            }
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

        String userIdStr = userId.toString();

        // 更新总排行榜
        redisUtil.zIncrBy(RedisKeyConstants.getUserActivityTotalRankKey(), userIdStr, score);

        // 更新日排行榜（1天过期）
        String dailyRankKey = getDayKey();
        redisUtil.zIncrBy(dailyRankKey, userIdStr, score);
        Long ttl = redisUtil.ttl(dailyRankKey);
        if (!NumUtil.upZero(ttl)) {
            redisUtil.expire(dailyRankKey, Duration.ofDays(1));
        }

        // 更新月排行榜（30天过期）
        String monthlyRankKey = getMonthKey();
        redisUtil.zIncrBy(monthlyRankKey, userIdStr, score);
        ttl = redisUtil.ttl(monthlyRankKey);
        if (!NumUtil.upZero(ttl)) {
            redisUtil.expire(monthlyRankKey, Duration.ofDays(30));
        }
    }

    /**
     * Hash方式：幂等性检查 + 积分限制校验 + 记录更新
     *
     * @param event 活跃度事件
     * @param score 积分值
     * @return 实际获得的积分
     */
    private Integer scoreIdempotency(UserActivityEvent event, Integer score) {
        if (score == 0) {
            return 0;
        }

        // 没有具体目标的操作（如每日签到）跳过幂等性检查，只做积分限制
        if (event.getTargetId() == null || event.getTargetType() == null) {
            return scoreDailyLimit(event.getUserId(), score);
        }

        String dayStr = getDateStr(DAY_FORMATTER);
        String userDayKey = RedisKeyConstants.getUserActivityDailyKey(event.getUserId(), dayStr);
        String operationField = "op:" + event.getActionType() + ":" + event.getTargetType() + ":" + event.getTargetId();

        // 检查操作是否已存在
        if (redisUtil.hExists(userDayKey, operationField)) {
            log.debug("操作已存在，幂等性检查失败: userId={}, operation={}", event.getUserId(), operationField);
            return 0;
        }

        // 统一积分处理
        int actualScore = scoreDailyLimit(event.getUserId(), score);
        if (actualScore != 0) {
            // 记录具体操作
            redisUtil.hIncrBy(userDayKey, operationField, actualScore);
        }

        log.debug("活跃度记录成功: userId={}, operation={}, score={}", event.getUserId(), operationField, actualScore);
        return actualScore;
    }

    private Integer scoreDailyLimit(Long userId, Integer score) {
        if (score == 0) {
            return 0;
        }

        String dayStr = getDateStr(DAY_FORMATTER);
        String userDayKey = RedisKeyConstants.getUserActivityDailyKey(userId, dayStr);
        int actualScore;

        if (score > 0) {
            // 正分检查限制
            String currentTotalStr = redisUtil.hGet(userDayKey, SCORE_TOTAL_FIELD, String.class);
            Integer currentTotal = (currentTotalStr == null) ? 0 : Integer.parseInt(currentTotalStr);

            actualScore = score;
            if (currentTotal + score > DAILY_SCORE_LIMIT) {
                actualScore = DAILY_SCORE_LIMIT - currentTotal;
                if (actualScore <= 0) {
                    return 0;
                }
            }
        } else {
            // 负分不做限制，直接记录
            actualScore = score;
        }

        // 更新总积分
        redisUtil.hIncrBy(userDayKey, SCORE_TOTAL_FIELD, actualScore);
        Long ttl = redisUtil.ttl(userDayKey);
        if (!NumUtil.upZero(ttl)) {
            redisUtil.expire(userDayKey, Duration.ofHours(25));
        }

        return actualScore;
    }

    private static @NotNull String getDateStr(DateTimeFormatter dayFormatter) {
        LocalDate now = LocalDate.now();
        return now.format(dayFormatter);
    }
}