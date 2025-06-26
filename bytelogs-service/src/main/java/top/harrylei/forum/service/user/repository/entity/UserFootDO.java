package top.harrylei.forum.service.user.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.entity.BaseDO;

import java.io.Serial;

/**
 * 用户足迹实体对象
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_foot")
@Accessors(chain = true)
public class UserFootDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

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
    private Integer contentType;

    /**
     * 内容所属用户ID
     */
    private Long contentUserId;

    /**
     * 收藏状态
     */
    private Integer collectionState;

    /**
     * 阅读状态
     */
    private Integer readState;

    /**
     * 评论状态
     */
    private Integer commentState;

    /**
     * 点赞状态
     */
    private Integer praiseState;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer deleted;
}