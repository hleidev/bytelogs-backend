package top.harrylei.community.service.notify.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.community.api.enums.article.ContentTypeEnum;
import top.harrylei.community.api.enums.notify.NotifyMsgStateEnum;
import top.harrylei.community.api.enums.notify.NotifyTypeEnum;
import top.harrylei.community.api.model.base.BaseDO;

import java.io.Serial;

/**
 * 通知消息实体对象
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notify_msg")
@Accessors(chain = true)
public class NotifyMsgDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联的主键ID（文章ID、评论ID等）
     */
    private Long relatedId;

    /**
     * 通知接收用户ID
     */
    private Long notifyUserId;

    /**
     * 触发操作的用户ID
     */
    private Long operateUserId;

    /**
     * 通知消息内容
     */
    private String msg;

    /**
     * 通知类型：1-评论，2-回复，3-点赞，4-收藏，5-关注，6-系统消息
     */
    private NotifyTypeEnum type;

    /**
     * 内容类型：0-不适用，1-文章，2-评论
     */
    private ContentTypeEnum contentType;

    /**
     * 阅读状态：0-未读，1-已读
     */
    private NotifyMsgStateEnum state;
}