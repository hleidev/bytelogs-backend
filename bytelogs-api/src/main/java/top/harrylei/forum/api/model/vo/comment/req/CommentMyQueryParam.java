package top.harrylei.forum.api.model.vo.comment.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.entity.BasePage;

/**
 * 我的评论分页查询参数
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "我的评论分页查询参数")
public class CommentMyQueryParam extends BasePage {
}