package top.harrylei.forum.api.model.notify.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.base.BaseDTO;
import top.harrylei.forum.api.enums.NotifyMsgStateEnum;
import top.harrylei.forum.api.enums.NotifyTypeEnum;
import top.harrylei.forum.api.enums.comment.ContentTypeEnum;

/**
 * 通知消息数据传输对象
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotifyMsgDTO extends BaseDTO {

    /**
     * 消息关联的主体，如文章、评论
     */
    private Long relatedId;

    /**
     * 关联信息
     */
    private String relatedInfo;

    /**
     * 发起消息的用户id
     */
    private Long operateUserId;

    /**
     * 发起消息的用户名
     */
    private String operateUserName;

    /**
     * 发起消息的用户头像
     */
    private String operateUserAvatar;

    /**
     * 消息类型
     */
    private NotifyTypeEnum type;

    /**
     * 内容类型
     */
    private ContentTypeEnum contentType;

    /**
     * 消息正文
     */
    private String msg;

    /**
     * 阅读状态
     */
    private NotifyMsgStateEnum state;

}
