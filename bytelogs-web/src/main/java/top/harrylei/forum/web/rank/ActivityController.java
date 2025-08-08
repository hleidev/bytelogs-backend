package top.harrylei.forum.web.rank;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.harrylei.forum.api.enums.ErrorCodeEnum;
import top.harrylei.forum.api.enums.rank.ActivityRankTypeEnum;
import top.harrylei.forum.api.model.base.ResVO;
import top.harrylei.forum.api.model.rank.dto.ActivityRankDTO;
import top.harrylei.forum.api.model.rank.vo.ActivityRankListVO;
import top.harrylei.forum.api.model.rank.vo.ActivityRankVO;
import top.harrylei.forum.api.model.rank.vo.ActivityStatsVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.rank.service.ActivityService;

import java.util.List;

/**
 * 用户活跃度控制器
 *
 * @author harry
 */
@Tag(name = "用户活跃排行榜模块", description = "提供用户活跃排行榜的功能")
@Slf4j
@RestController
@RequestMapping("/v1/rank/activity")
@RequiredArgsConstructor
@Validated
public class ActivityController {

    private final ActivityService activityService;

    /**
     * 获取活跃度排行榜
     *
     * @param rankType 排行榜类型
     * @return 排行榜数据，包含用户个人排名
     */
    @Operation(summary = "获取活跃度排行榜", description = "获取活跃度排行榜，登录用户会额外返回个人排名信息")
    @GetMapping
    public ResVO<ActivityRankListVO> getRank(@NotNull(message = "排行榜类型不能为空") Integer rankType) {
        // 获取排行榜列表
        ActivityRankTypeEnum rankTypeEnum = ActivityRankTypeEnum.fromCode(rankType);
        List<ActivityRankDTO> rankList = activityService.listRank(rankTypeEnum);

        // 构建响应对象
        ActivityRankListVO result = new ActivityRankListVO().setRankList(rankList);

        // 如果用户已登录，获取个人排名信息
        Long currentUserId = ReqInfoContext.getContext().getUserId();
        if (currentUserId != null) {
            ActivityRankVO userRank = activityService.getUserRank(currentUserId, rankTypeEnum);
            result.setCurrentUserRank(userRank);
        }

        return ResVO.ok(result);
    }

    /**
     * 获取用户积分统计概览
     *
     * @return 用户在日榜、月榜、总榜的排名和积分信息
     */
    @Operation(summary = "获取用户积分", description = "获取当前登录用户在日榜、月榜、总榜的排名和积分信息")
    @GetMapping("/stats")
    public ResVO<ActivityStatsVO> getActivityStats() {
        Long currentUserId = ReqInfoContext.getContext().getUserId();
        ExceptionUtil.errorIf(currentUserId == null, ErrorCodeEnum.UNAUTHORIZED);

        ActivityStatsVO stats = activityService.getUserStats(currentUserId);
        return ResVO.ok(stats);
    }

    /**
     * 获取历史排行榜
     *
     * @param rankType 排行榜类型
     * @param period   排行榜期间（如：2025-01-10, 2025-01, total）
     * @return 历史排行榜数据，包含用户个人排名
     */
    @Operation(summary = "获取历史排行榜", description = "获取指定期间的历史排行榜，登录用户会额外返回个人排名信息")
    @GetMapping("/history")
    public ResVO<ActivityRankListVO> getHistoryRank(@NotNull(message = "排行榜类型不能为空") Integer rankType,
                                                    @NotNull(message = "排行榜期间不能为空") String period) {
        // 获取历史排行榜列表
        ActivityRankTypeEnum rankTypeEnum = ActivityRankTypeEnum.fromCode(rankType);
        List<ActivityRankDTO> rankList = activityService.listRank(rankTypeEnum, period);

        // 构建响应对象
        ActivityRankListVO result = new ActivityRankListVO().setRankList(rankList);

        // 如果用户已登录，获取个人历史排名信息
        Long currentUserId = ReqInfoContext.getContext().getUserId();
        if (currentUserId != null) {
            ActivityRankVO userRank = activityService.getUserRank(currentUserId, rankTypeEnum, period);
            result.setCurrentUserRank(userRank);
        }

        return ResVO.ok(result);
    }
}