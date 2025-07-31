package top.harrylei.forum.web.rank;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户活跃度控制器
 *
 * @author harry
 */
@Tag(name = "用户活跃排行榜模块", description = "提供用户活跃排行榜的功能")
@Slf4j
@RestController
@RequestMapping("/api/v1/rank/activity")
@RequiredArgsConstructor
@Validated
public class ActivityController {
}