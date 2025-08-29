package top.harrylei.community.service.notify.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.harrylei.community.api.model.notify.dto.NotifyMsgDTO;
import top.harrylei.community.api.model.notify.vo.NotifyMsgVO;
import top.harrylei.community.service.notify.repository.entity.NotifyMsgDO;

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
}