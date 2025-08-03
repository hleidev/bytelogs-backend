package top.harrylei.forum.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.harrylei.forum.api.enums.rank.ActivityRankTypeEnum;
import top.harrylei.forum.api.model.base.ResVO;
import top.harrylei.forum.core.security.permission.RequiresAdmin;
import top.harrylei.forum.service.rank.service.ActivityService;

/**
 * 活跃度排行榜管理模块
 *
 * @author harry
 */
@Tag(name = "活跃度排行榜管理模块", description = "提供活跃度排行榜后台管理接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/rank/activity")
@RequiredArgsConstructor
@RequiresAdmin
@Validated
public class ActivityRankManagementController {

    private final ActivityService activityService;

    /**
     * 备份所有排行榜数据
     *
     * @return 操作结果
     */
    @Operation(summary = "备份所有排行榜", description = "手动触发备份所有类型的排行榜数据到数据库")
    @PostMapping("/backup")
    public ResVO<Void> backupAllRankingData() {
        activityService.backupAllRankingData();
        return ResVO.ok();
    }

    /**
     * 备份指定类型排行榜数据
     *
     * @param rankType 排行榜类型
     * @return 操作结果
     */
    @Operation(summary = "备份指定排行榜", description = "手动触发备份指定类型的排行榜数据到数据库")
    @PostMapping("/backup/{rankType}")
    public ResVO<Void> backupRankingData(@PathVariable ActivityRankTypeEnum rankType) {
        activityService.backupRankingData(rankType);
        return ResVO.ok();
    }
}