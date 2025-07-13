package top.harrylei.forum.service.notify.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.harrylei.forum.api.model.vo.notify.dto.NotifyMsgDTO;
import top.harrylei.forum.service.notify.repository.entity.NotifyMsgDO;

/**
 * 通知消息结构映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring")
public interface NotifyMsgStructMapper {

    @Mapping(target = "msgId", source = "id")
    @Mapping(target = "relatedInfo", ignore = true)
    @Mapping(target = "operateUserName", ignore = true)
    @Mapping(target = "operateUserAvatar", ignore = true)
    NotifyMsgDTO toDTO(NotifyMsgDO notifyMsg);
}