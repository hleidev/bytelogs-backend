package top.harrylei.community.api.model.notify.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.community.api.model.base.BasePage;
import top.harrylei.community.api.enums.notify.NotifyMsgStateEnum;
import top.harrylei.community.api.enums.notify.NotifyTypeEnum;

/**
 * 通知消息分页查询参数
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "通知消息分页查询参数")
public class NotifyMsgQueryParam extends BasePage {

    @Schema(description = "消息状态", example = "1")
    private NotifyMsgStateEnum state;

    @Schema(description = "通知类型", example = "1")
    private NotifyTypeEnum notifyType;
}