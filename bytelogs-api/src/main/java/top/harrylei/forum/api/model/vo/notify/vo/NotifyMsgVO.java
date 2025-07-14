package top.harrylei.forum.api.model.vo.notify.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.entity.BaseVO;
import top.harrylei.forum.api.model.enums.NotifyTypeEnum;
import top.harrylei.forum.api.model.enums.comment.ContentTypeEnum;

/**
 * 通知消息展示对象
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通知消息展示对象")
public class NotifyMsgVO extends BaseVO {

    /**
     * 消息关联的主体，如文章、评论
     */
    @Schema(description = "关联内容ID", example = "456")
    private Long relatedId;

    /**
     * 关联信息
     */
    @Schema(description = "关联信息", example = "Spring Boot 实战教程")
    private String relatedInfo;

    /**
     * 发起消息的用户id
     */
    @Schema(description = "操作用户ID", example = "789")
    private Long operateUserId;

    /**
     * 发起消息的用户名
     */
    @Schema(description = "操作用户名", example = "张三")
    private String operateUserName;

    /**
     * 发起消息的用户头像
     */
    @Schema(description = "操作用户头像", example = "https://cdn.bytelogs.com/avatar.jpg")
    private String operateUserAvatar;

    /**
     * 消息类型
     */
    @Schema(description = "消息类型")
    private NotifyTypeEnum type;

    /**
     * 内容类型
     */
    @Schema(description = "内容类型")
    private ContentTypeEnum contentType;

    /**
     * 消息正文
     */
    @Schema(description = "消息正文", example = "张三 点赞了你的文章")
    private String msg;

    /**
     * 阅读状态：0-未读，1-已读
     */
    @Schema(description = "阅读状态", example = "0")
    private Integer state;
}