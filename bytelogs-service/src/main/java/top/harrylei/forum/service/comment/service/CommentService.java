package top.harrylei.forum.service.comment.service;

import top.harrylei.forum.api.enums.user.OperateTypeEnum;
import top.harrylei.forum.api.model.comment.dto.CommentDTO;
import top.harrylei.forum.api.model.comment.req.CommentMyQueryParam;
import top.harrylei.forum.api.model.comment.req.CommentQueryParam;
import top.harrylei.forum.api.model.comment.vo.CommentMyVO;
import top.harrylei.forum.api.model.comment.vo.TopCommentVO;
import top.harrylei.forum.api.model.page.PageVO;

/**
 * 评论服务接口
 *
 * @author harry
 */
public interface CommentService {

    /**
     * 保存评论
     *
     * @param dto 评论DTO
     */
    Long saveComment(CommentDTO dto);

    /**
     * 分页查询
     *
     * @param param 分页查询参数
     * @return 分页结果
     */
    PageVO<TopCommentVO> pageQuery(CommentQueryParam param);

    /**
     * 编辑评论
     *
     * @param dto 编辑数据传输对象
     */
    void updateComment(CommentDTO dto);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     */
    void deleteComment(Long commentId);

    /**
     * 恢复评论
     *
     * @param commentId 评论ID
     */
    void restoreComment(Long commentId);

    /**
     * 查询用户评论
     *
     * @param userId 用户ID
     * @param param  分页查询参数
     * @return 分页结果
     */
    PageVO<CommentMyVO> pageQueryUserComments(Long userId, CommentMyQueryParam param);

    /**
     * 评论操作
     *
     * @param commentId 评论ID
     * @param type      操作类型
     */
    void actionComment(Long commentId, OperateTypeEnum type);
}
