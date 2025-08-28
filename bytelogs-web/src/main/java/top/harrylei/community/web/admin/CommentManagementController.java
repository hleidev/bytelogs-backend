package top.harrylei.community.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.harrylei.community.api.model.base.ResVO;
import top.harrylei.community.api.model.comment.req.CommentManagementQueryParam;
import top.harrylei.community.api.model.comment.vo.CommentManagementVO;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.core.security.permission.RequiresAdmin;
import top.harrylei.community.service.comment.service.CommentManagementService;

import java.util.List;

/**
 * 评论管理控制器
 *
 * @author harry
 */
@Tag(name = "评论管理模块", description = "提供评论后台管理接口")
@Slf4j
@RestController
@RequestMapping("/v1/admin/comment")
@RequiredArgsConstructor
@RequiresAdmin
@Validated
public class CommentManagementController {

    private final CommentManagementService commentManagementService;

    /**
     * 管理端分页查询评论
     */
    @Operation(summary = "分页查询", description = "为管理后台提供评论分页查询功能，支持多维度筛选和排序")
    @GetMapping("/page")
    public ResVO<PageVO<CommentManagementVO>> pageQuery(@Valid CommentManagementQueryParam queryParam) {
        PageVO<CommentManagementVO> result = commentManagementService.pageQuery(queryParam);
        return ResVO.ok(result);
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论", description = "管理员删除评论，支持单个和批量操作")
    @DeleteMapping
    public ResVO<Void> deleteComments(@NotNull(message = "评论ID列表不能为空") @RequestBody List<Long> commentIds) {
        commentManagementService.deleteComments(commentIds);
        return ResVO.ok();
    }

    /**
     * 恢复评论
     */
    @Operation(summary = "恢复评论", description = "管理员恢复已删除评论，支持单个和批量操作")
    @PutMapping("/restore")
    public ResVO<Void> restoreComments(@NotNull(message = "评论ID列表不能为空") @RequestBody List<Long> commentIds) {
        commentManagementService.restoreComments(commentIds);
        return ResVO.ok();
    }
}