package top.harrylei.forum.api.model.vo.comment.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论树状结构
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SubCommentDTO extends BaseCommentDTO {

    /**
     * 父评论内容
     */
    private String parentContent;
}
