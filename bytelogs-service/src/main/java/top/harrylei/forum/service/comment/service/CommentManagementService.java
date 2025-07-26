package top.harrylei.forum.service.comment.service;

import top.harrylei.forum.api.model.comment.req.CommentManagementQueryParam;
import top.harrylei.forum.api.model.comment.vo.CommentManagementVO;
import top.harrylei.forum.api.model.page.PageVO;

import java.util.List;

/**
 * 评论管理服务接口
 *
 * @author harry
 */
public interface CommentManagementService {

    /**
     * 管理端分页查询评论
     *
     * @param queryParam 查询参数
     * @return 分页结果
     */
    PageVO<CommentManagementVO> pageQuery(CommentManagementQueryParam queryParam);

    /**
     * 管理员删除评论
     *
     * @param commentIds 评论ID列表
     */
    void deleteComments(List<Long> commentIds);

    /**
     * 管理员恢复评论
     *
     * @param commentIds 评论ID列表
     */
    void restoreComments(List<Long> commentIds);
}