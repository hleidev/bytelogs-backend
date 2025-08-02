package top.harrylei.forum.service.rank.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import top.harrylei.forum.api.enums.rank.ActivityActionEnum;
import top.harrylei.forum.api.enums.rank.ActivityRankTypeEnum;
import top.harrylei.forum.api.event.ActivityRankEvent;
import top.harrylei.forum.api.model.rank.dto.ActivityRankDTO;
import top.harrylei.forum.api.model.rank.vo.ActivityRankVO;
import top.harrylei.forum.api.model.rank.vo.ActivityStatsVO;
import top.harrylei.forum.api.model.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;
import top.harrylei.forum.core.util.NumUtil;
import top.harrylei.forum.core.util.RedisUtil;
import top.harrylei.forum.service.rank.service.ActivityService;
import top.harrylei.forum.service.user.service.UserService;
import top.harrylei.forum.service.user.service.cache.UserCacheService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户活跃度服务实现
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final RedisUtil redisUtil;

    private static final String SCORE_TOTAL_FIELD = "score_total";
    private static final Integer DAILY_SCORE_LIMIT = 100;
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private final UserService userService;
    private final UserCacheService userCacheService;

    private static String getDayKey() {
        String dayStr = getDateStr();
        return RedisKeyConstants.getActivityDailyRankKey(dayStr);
    }

    private static String getMonthKey() {
        LocalDate now = LocalDate.now();
        String monthStr = now.format(MONTH_FORMATTER);
        return RedisKeyConstants.getActivityMonthlyRankKey(monthStr);
    }

    @Override
    public void handleActivityEvent(ActivityRankEvent event) {
        try {
            // 基础验证
            ActivityActionEnum actionEnum = ActivityActionEnum.fromCode(event.getActionType());
            if (actionEnum == null) {
                log.error("无效的活跃度行为类型: actionType={}", event.getActionType());
                return;
            }

            if (actionEnum.getScore() == 0) {
                log.debug("积分为0，跳过处理: userId={}, action={}", event.getUserId(), event.getActionType());
                return;
            }

            // 根据积分类型分发处理
            if (actionEnum.getScore() < 0) {
                handleNegativeScore(event);
            } else {
                handlePositiveScore(event, actionEnum);
            }

        } catch (Exception e) {
            log.error("处理活跃度事件失败: userId={}, action={}", event.getUserId(), event.getActionType(), e);
            throw e;
        }
    }

    /**
     * 处理正分事件
     */
    private void handlePositiveScore(ActivityRankEvent event, ActivityActionEnum actionEnum) {
        // 幂等性检查
        if (isOperationDuplicate(event)) {
            log.debug("重复操作，跳过: userId={}, action={}", event.getUserId(), event.getActionType());
            return;
        }

        // 积分计算与限制
        Integer actualScore = calculateActualScore(event.getUserId(), actionEnum.getScore());
        if (actualScore == 0) {
            log.debug("积分计算结果为0，跳过: userId={}, baseScore={}", event.getUserId(), actionEnum.getScore());
            return;
        }

        // 执行积分更新
        recordOperation(event, actualScore);
        updateUserScore(event.getUserId(), actualScore);

        log.debug("正分事件处理完成: userId={}, action={}, baseScore={}, actualScore={}",
                  event.getUserId(), event.getActionType(), actionEnum.getScore(), actualScore);
    }

    @Override
    public void updateUserScore(Long userId, Integer score) {
        if (score == 0) {
            return;
        }

        String userIdStr = userId.toString();

        // 更新总排行榜
        redisUtil.zIncrBy(RedisKeyConstants.getActivityTotalRankKey(), userIdStr, score);

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
            redisUtil.expire(monthlyRankKey, Duration.ofDays(31));
        }
    }

    @Override
    public List<ActivityRankDTO> listRank(ActivityRankTypeEnum rankType) {
        String rankKey = getRankKey(rankType);
        if (rankKey == null) {
            return List.of();
        }

        // 一次性获取排行榜数据
        List<Map.Entry<String, Double>> rankedList = redisUtil.zRevRangeWithScores(rankKey, 0, 99);
        if (rankedList.isEmpty()) {
            return List.of();
        }

        // 提取用户ID列表
        List<Long> userIds = rankedList.stream()
                .map(member -> Long.valueOf(member.getKey()))
                .toList();

        // 批量查询用户信息
        List<UserInfoDetailDTO> userInfoList = userService.batchQueryUserInfo(userIds);
        Map<Long, UserInfoDetailDTO> userInfoMap = userInfoList.stream()
                .collect(Collectors.toMap(UserInfoDetailDTO::getUserId, Function.identity()));

        // 构建排行榜结果
        List<ActivityRankDTO> result = new ArrayList<>();
        AtomicInteger rank = new AtomicInteger(1);
        for (Map.Entry<String, Double> member : rankedList) {
            Long userId = Long.valueOf(member.getKey());
            UserInfoDetailDTO userInfo = userInfoMap.get(userId);

            if (userInfo != null) {
                result.add(new ActivityRankDTO()
                                   .setUserId(userId)
                                   .setUserName(userInfo.getUserName())
                                   .setAvatar(userInfo.getAvatar())
                                   .setScore(member.getValue().intValue())
                                   .setRank(rank.getAndIncrement()));
            }
        }
        return result;
    }

    @Override
    public ActivityRankVO getUserRank(Long userId, ActivityRankTypeEnum rankType) {
        // 获取排名和积分
        Integer[] rankScore = getUserRankScore(userId, rankType);
        if (rankScore == null) {
            return null;
        }

        // 获取用户信息
        UserInfoDetailDTO userInfo = userCacheService.getUserInfo(userId);
        if (userInfo == null) {
            return null;
        }

        return new ActivityRankVO()
                .setUserId(userId)
                .setUserName(userInfo.getUserName())
                .setAvatar(userInfo.getAvatar())
                .setRank(rankScore[0])
                .setScore(rankScore[1]);
    }

    /**
     * 检查操作是否重复
     *
     * @param event 活跃度事件
     * @return true-重复操作，false-非重复操作
     */
    private boolean isOperationDuplicate(ActivityRankEvent event) {
        // TODO 没有具体目标的操作（如每日签到）跳过幂等性检查
        if (event.getTargetId() == null || event.getTargetType() == null) {
            return false;
        }

        String dayStr = getDateStr();
        String userDayKey = RedisKeyConstants.getActivityDailyKey(event.getUserId(), dayStr);
        String operationField = buildOperationField(event);

        return redisUtil.hExists(userDayKey, operationField);
    }

    /**
     * 计算实际获得的积分
     *
     * @param userId    用户ID
     * @param baseScore 基础积分
     * @return 实际积分
     */
    private Integer calculateActualScore(Long userId, Integer baseScore) {
        // 负分不受每日限制约束，直接返回
        if (baseScore < 0) {
            return baseScore;
        }

        // 正分需要检查每日限制
        String dayStr = getDateStr();
        String userDayKey = RedisKeyConstants.getActivityDailyKey(userId, dayStr);

        Integer currentTotal = redisUtil.hGet(userDayKey, SCORE_TOTAL_FIELD, Integer.class);
        int totalScore = (currentTotal == null) ? 0 : currentTotal;

        // 检查是否超过每日限制
        if (totalScore + baseScore > DAILY_SCORE_LIMIT) {
            int resultScore = DAILY_SCORE_LIMIT - totalScore;
            return Math.max(resultScore, 0);
        }

        return baseScore;
    }

    /**
     * 记录操作历史和更新积分统计
     *
     * @param event       活跃度事件
     * @param actualScore 实际积分
     */
    private void recordOperation(ActivityRankEvent event, Integer actualScore) {
        String dayStr = getDateStr();
        String userDayKey = RedisKeyConstants.getActivityDailyKey(event.getUserId(), dayStr);

        // 更新总积分统计
        redisUtil.hIncrBy(userDayKey, SCORE_TOTAL_FIELD, actualScore);

        // 记录具体操作
        if (event.getTargetId() != null && event.getTargetType() != null) {
            String operationField = buildOperationField(event);
            redisUtil.hIncrBy(userDayKey, operationField, actualScore);
        }

        // 设置过期时间
        Long ttl = redisUtil.ttl(userDayKey);
        if (!NumUtil.upZero(ttl)) {
            redisUtil.expire(userDayKey, Duration.ofHours(25));
        }
    }

    /**
     * 构建操作字段名
     *
     * @param event 活跃度事件
     * @return 操作字段名
     */
    private String buildOperationField(ActivityRankEvent event) {
        return "op:" + event.getActionType() + ":" + event.getTargetType() + ":" + event.getTargetId();
    }

    /**
     * 处理负分操作
     * 简单逻辑：找到对应的正分记录，删除并扣分
     *
     * @param event 活跃度事件
     */
    private void handleNegativeScore(ActivityRankEvent event) {
        String dayStr = getDateStr();
        String userDayKey = RedisKeyConstants.getActivityDailyKey(event.getUserId(), dayStr);

        // 1. 构建对应的正分操作字段名
        String positiveOperationField = buildPositiveOperationField(event);

        // 2. 检查原有正分操作是否存在
        Integer originalScore = redisUtil.hGet(userDayKey, positiveOperationField, Integer.class);
        if (originalScore == null || originalScore <= 0) {
            log.debug("未找到对应的正分操作，跳过: userId={}, action={}", event.getUserId(), event.getActionType());
            return;
        }

        // 3. 删除正分记录
        redisUtil.hDel(userDayKey, positiveOperationField);

        // 4. 从每日积分统计中扣除
        redisUtil.hDecrBy(userDayKey, SCORE_TOTAL_FIELD, originalScore);

        // 5. 更新排行榜积分
        updateUserScore(event.getUserId(), -originalScore);

        log.debug("负分事件处理成功: userId={}, action={}, deductedScore={}",
                  event.getUserId(), event.getActionType(), -originalScore);
    }

    /**
     * 构建对应的正分操作字段名
     *
     * @param event 负分事件
     * @return 对应的正分操作字段名
     */
    private String buildPositiveOperationField(ActivityRankEvent event) {
        Integer positiveActionType = getCorrespondingPositiveAction(event.getActionType());
        if (positiveActionType == null) {
            return null;
        }

        return "op:" + positiveActionType + ":" + event.getTargetType() + ":" + event.getTargetId();
    }

    /**
     * 获取负分操作对应的正分操作类型
     *
     * @param negativeActionType 负分操作类型
     * @return 对应的正分操作类型，若无对应关系则返回null
     */
    private Integer getCorrespondingPositiveAction(Integer negativeActionType) {
        ActivityActionEnum actionEnum = ActivityActionEnum.fromCode(negativeActionType);
        if (actionEnum == null) {
            return null;
        }

        return switch (actionEnum) {
            case CANCEL_PRAISE -> ActivityActionEnum.PRAISE.getCode();
            case CANCEL_COLLECT -> ActivityActionEnum.COLLECT.getCode();
            case CANCEL_FOLLOW -> ActivityActionEnum.FOLLOW.getCode();
            case DELETE_COMMENT -> ActivityActionEnum.COMMENT.getCode();
            case DELETE_ARTICLE -> ActivityActionEnum.ARTICLE.getCode();
            default -> null;
        };
    }

    /**
     * 根据排行榜类型获取对应的Redis键
     *
     * @param rankType 排行榜类型
     * @return Redis键，无效类型返回null
     */
    private String getRankKey(ActivityRankTypeEnum rankType) {
        if (rankType == null) {
            log.error("排行榜类型不能为空");
            return null;
        }

        return switch (rankType) {
            case TOTAL -> RedisKeyConstants.getActivityTotalRankKey();
            case MONTHLY -> getMonthKey();
            case DAILY -> getDayKey();
        };
    }

    @Override
    public ActivityStatsVO getUserStats(Long userId) {
        if (userId == null) {
            return null;
        }

        ActivityStatsVO stats = new ActivityStatsVO();

        // 获取日榜数据
        Integer[] dailyRankScore = getUserRankScore(userId, ActivityRankTypeEnum.DAILY);
        if (dailyRankScore != null) {
            stats.setDailyRank(dailyRankScore[0]).setDailyScore(dailyRankScore[1]);
        }

        // 获取月榜数据
        Integer[] monthlyRankScore = getUserRankScore(userId, ActivityRankTypeEnum.MONTHLY);
        if (monthlyRankScore != null) {
            stats.setMonthlyRank(monthlyRankScore[0]).setMonthlyScore(monthlyRankScore[1]);
        }

        // 获取总榜数据
        Integer[] totalRankScore = getUserRankScore(userId, ActivityRankTypeEnum.TOTAL);
        if (totalRankScore != null) {
            stats.setTotalRank(totalRankScore[0]).setTotalScore(totalRankScore[1]);
        }

        return stats;
    }

    /**
     * 获取用户在指定排行榜中的排名和积分
     *
     * @param userId   用户ID
     * @param rankType 排行榜类型
     * @return [排名, 积分]数组，无数据时返回null
     */
    private Integer[] getUserRankScore(Long userId, ActivityRankTypeEnum rankType) {
        if (userId == null || rankType == null) {
            return null;
        }

        String rankKey = getRankKey(rankType);
        if (rankKey == null) {
            return null;
        }

        String userIdStr = userId.toString();
        Double score = redisUtil.zScore(rankKey, userIdStr);
        if (score == null) {
            return null;
        }

        Long rank = redisUtil.zRevRank(rankKey, userIdStr);
        if (rank == null) {
            return null;
        }

        return new Integer[]{(int) (rank + 1), score.intValue()};
    }

    private static @NotNull String getDateStr() {
        LocalDate now = LocalDate.now();
        return now.format(DAY_FORMATTER);
    }
}