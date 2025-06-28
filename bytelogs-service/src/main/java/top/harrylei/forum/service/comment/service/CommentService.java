package top.harrylei.forum.service.comment.service;

import top.harrylei.forum.api.model.vo.comment.dto.CommentDTO;
import top.harrylei.forum.api.model.vo.comment.dto.TopCommentDTO;
import top.harrylei.forum.api.model.vo.comment.req.CommentQueryParam;
import top.harrylei.forum.api.model.vo.page.PageVO;

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
    PageVO<TopCommentDTO> pageQuery(CommentQueryParam param);
}
