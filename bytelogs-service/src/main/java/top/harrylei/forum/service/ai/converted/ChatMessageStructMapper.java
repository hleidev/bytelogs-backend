package top.harrylei.forum.service.ai.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.harrylei.forum.api.model.ai.dto.ChatMessageDTO;
import top.harrylei.forum.api.model.ai.vo.ChatMessageVO;
import top.harrylei.forum.service.ai.repository.entity.ChatMessageDO;

/**
 * 消息数据转换器
 *
 * @author harry
 */
@Mapper(componentModel = "spring")
public interface ChatMessageStructMapper {

    /**
     * DO转DTO - 字段映射
     */
    @Mapping(source = "messageType", target = "role")
    @Mapping(source = "provider", target = "vendor")
    @Mapping(source = "modelName", target = "model")
    ChatMessageDTO toDTO(ChatMessageDO chatMessageDO);

    /**
     * DTO转VO
     */
    ChatMessageVO toVO(ChatMessageDTO chatMessageDTO);
}