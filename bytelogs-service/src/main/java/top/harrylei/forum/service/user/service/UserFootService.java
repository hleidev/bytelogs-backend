package top.harrylei.forum.service.user.service;

import top.harrylei.forum.api.model.enums.OperateTypeEnum;
import top.harrylei.forum.api.model.enums.comment.ContentTypeEnum;
import top.harrylei.forum.api.model.vo.user.dto.UserFootDTO;
import top.harrylei.forum.service.comment.repository.entity.CommentDO;

/**
 * 用户足迹服务接口
 *
 * @author harry
 */
public interface UserFootService {

    /**
     * 保存评论足迹
     *
     * @param comment               保存评论
     * @param articleAuthorId       文章作者
     * @param parentCommentAuthorId 父评论作者
     */
    void saveCommentFoot(CommentDO comment, Long articleAuthorId, Long parentCommentAuthorId);

    /**
     * 删除评论足迹
     *
     * @param comment               删除评论
     * @param articleAuthorId       文章作者
     * @param parentCommentAuthorId 父评论作者
     */
    void deleteCommentFoot(CommentDO comment, Long articleAuthorId, Long parentCommentAuthorId);

    /**
     * 查询用户足迹
     *
     * @param userId          用户ID
     * @param contentId       内容ID
     * @param contentTypeEnum 内容类型
     * @return 用户足迹传输对象
     */
    UserFootDTO getUserFoot(Long userId, Long contentId, ContentTypeEnum contentTypeEnum);

    /**
     * 评论操作
     *
     * @param userId          用户ID
     * @param type            操作类型：点赞、收藏等
     * @param commentAuthorId 评论作者ID
     * @param commentId       评论ID
     * @return 是否成功操作
     */
    Boolean actionComment(Long userId,
                          OperateTypeEnum type,
                          Long commentAuthorId,
                          Long commentId);

    /**
     * 文章操作
     *
     * @param userId         用户ID
     * @param type           操作类型：点赞、收藏等
     * @param articleAuthorId 文章作者ID
     * @param articleId      文章ID
     * @return 是否成功操作
     */
    Boolean actionArticle(Long userId,
                          OperateTypeEnum type,
                          Long articleAuthorId,
                          Long articleId);
}