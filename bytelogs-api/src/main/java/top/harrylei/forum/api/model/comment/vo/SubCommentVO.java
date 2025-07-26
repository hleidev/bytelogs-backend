package top.harrylei.forum.api.model.comment.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 子评论视图对象
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SubCommentVO extends BaseCommentVO {

    /**
     * 父评论内容
     */
    private String parentContent;
}