package top.harrylei.forum.web.comment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.comment.dto.CommentDTO;
import top.harrylei.forum.api.model.vo.comment.req.CommentSaveReq;
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
}