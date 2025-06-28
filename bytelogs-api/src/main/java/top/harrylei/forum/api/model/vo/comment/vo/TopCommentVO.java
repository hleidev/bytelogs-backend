package top.harrylei.forum.api.model.vo.comment.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 顶级评论视图对象
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TopCommentVO extends BaseCommentVO {
    /**
     * 评论数量
     */
    private Integer commentCount;

    /**
     * 子评论
     */
    private List<SubCommentVO> childComments;

    public List<SubCommentVO> getChildComments() {
        if (childComments == null) {
            childComments = new ArrayList<>();
        }
        return childComments;
    }
}