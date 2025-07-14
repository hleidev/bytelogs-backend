package top.harrylei.forum.service.notify.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.harrylei.forum.api.model.enums.NotifyTypeEnum;
import top.harrylei.forum.api.model.enums.comment.ContentTypeEnum;
import top.harrylei.forum.api.model.vo.notify.dto.NotifyMsgDTO;
import top.harrylei.forum.api.model.vo.notify.vo.NotifyMsgVO;
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


    default NotifyTypeEnum map(Integer code) {
        return code != null ? NotifyTypeEnum.fromCode(code) : null;
    }

    default ContentTypeEnum mapContentType(Integer code) {
        return code != null ? ContentTypeEnum.fromCode(code) : null;
    }
}