package top.harrylei.forum.web.comment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.comment.dto.CommentDTO;
import top.harrylei.forum.api.model.vo.comment.req.CommentQueryParam;
import top.harrylei.forum.api.model.vo.comment.req.CommentSaveReq;
import top.harrylei.forum.api.model.vo.comment.vo.TopCommentVO;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.security.permission.RequiresLogin;
import top.harrylei.forum.service.comment.converted.CommentStructMapper;
import top.harrylei.forum.service.comment.service.CommentService;

/**
 * 评论相关模块
 *
 * @author harry
 */
@Tag(name = "评论相关模块", description = "提供评论的基础功能")
@Slf4j
@RestController
@RequestMapping("/api/v1/comment")
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final CommentService commentService;
    private final CommentStructMapper commentStructMapper;

    /**
     * 保存评论
     *
     * @param req 保存评论请求
     * @return 操作结果
     */
    @Operation(summary = "保存评论", description = "用户保存评论信息")
    @RequiresLogin
    @PostMapping
    public ResVO<Long> save(@Valid @RequestBody CommentSaveReq req) {
        CommentDTO dto = commentStructMapper.toDTO(req);
        dto.setUserId(ReqInfoContext.getContext().getUserId());
        Long commentId = commentService.saveComment(dto);
        return ResVO.ok(commentId);
    }

    /**
     * 分页查询
     *
     * @param param 分页查询参数
     * @return 分页结果
     */
    @Operation(summary = "分页查询", description = "用户评论分页查询")
    @GetMapping("/page")
    public ResVO<PageVO<TopCommentVO>> page(@Valid CommentQueryParam param) {
        PageVO<TopCommentVO> result = commentService.pageQuery(param);
        return ResVO.ok(result);
    }

    /**
     * 编辑评论
     *
     * @param commentId      评论ID
     * @param commentContent 编辑评论内容
     * @return 操作结果
     */
    @Operation(summary = "编辑评论", description = "编辑自己的评论内容（限制编辑时间窗口）")
    @RequiresLogin
    @PutMapping("/{commentId}")
    public ResVO<Void> update(@NotNull(message = "评论ID不能为空") @PathVariable Long commentId,
                              @NotBlank(message = "评论内容不能为空")
                              @Size(min = 1, max = 500, message = "评论内容长度必须在1-500字符之间")
                              @RequestBody String commentContent) {
        CommentDTO dto = new CommentDTO();
        dto.setId(commentId);
        dto.setContent(commentContent);
        commentService.updateComment(dto);
        return ResVO.ok();
    }
}