package top.harrylei.forum.service.comment.service;

import top.harrylei.forum.api.model.vo.comment.req.CommentManagementQueryParam;
import top.harrylei.forum.api.model.vo.comment.vo.CommentManagementVO;
import top.harrylei.forum.api.model.vo.page.PageVO;

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
}