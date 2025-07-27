package top.harrylei.forum.service.notify.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.harrylei.forum.api.enums.notify.NotifyMsgStateEnum;
import top.harrylei.forum.api.enums.notify.NotifyTypeEnum;
import top.harrylei.forum.api.enums.comment.ContentTypeEnum;
import top.harrylei.forum.api.model.notify.dto.NotifyMsgDTO;
import top.harrylei.forum.api.model.notify.vo.NotifyMsgVO;
import top.harrylei.forum.service.notify.repository.entity.NotifyMsgDO;

/**
 * 通知消息结构映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring")
public interface NotifyMsgStructMapper {

    @Mapping(target = "relatedInfo", ignore = true)
    @Mapping(target = "operateUserName", ignore = true)
    @Mapping(target = "operateUserAvatar", ignore = true)
    NotifyMsgDTO toDTO(NotifyMsgDO notifyMsg);

    NotifyMsgVO toVO(NotifyMsgDTO dto);

    default NotifyTypeEnum mapType(Integer code) {
        return code != null ? NotifyTypeEnum.fromCode(code) : null;
    }

    default ContentTypeEnum mapContentType(Integer code) {
        return code != null ? ContentTypeEnum.fromCode(code) : null;
    }

    default NotifyMsgStateEnum mapState(Integer code) {
        return code != null ? NotifyMsgStateEnum.fromCode(code) : null;
    }
}