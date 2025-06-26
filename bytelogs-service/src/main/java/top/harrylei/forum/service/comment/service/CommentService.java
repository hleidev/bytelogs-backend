package top.harrylei.forum.service.comment.service;

import top.harrylei.forum.api.model.vo.comment.dto.CommentDTO;

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
}