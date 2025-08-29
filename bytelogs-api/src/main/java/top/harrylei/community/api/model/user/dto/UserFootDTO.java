package top.harrylei.community.api.model.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.community.api.model.base.BaseDTO;
import top.harrylei.community.api.enums.article.CollectionStatusEnum;
import top.harrylei.community.api.enums.user.PraiseStatusEnum;
import top.harrylei.community.api.enums.user.ReadStatusEnum;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.comment.CommentStatusEnum;
import top.harrylei.community.api.enums.article.ContentTypeEnum;

/**
 * 用户足迹DTO
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserFootDTO extends BaseDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 内容类型：1-文章，2-评论
     */
    private ContentTypeEnum contentType;

    /**
     * 内容所属用户ID
     */
    private Long contentUserId;

    /**
     * 收藏状态
     */
    private CollectionStatusEnum collectionState;

    /**
     * 阅读状态
     */
    private ReadStatusEnum readState;

    /**
     * 评论状态
     */
    private CommentStatusEnum commentState;

    /**
     * 点赞状态
     */
    private PraiseStatusEnum praiseState;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private DeleteStatusEnum deleted;
}