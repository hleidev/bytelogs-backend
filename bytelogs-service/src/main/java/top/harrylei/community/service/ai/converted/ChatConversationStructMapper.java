package top.harrylei.community.service.ai.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.harrylei.community.api.model.ai.dto.ChatConversationDTO;
import top.harrylei.community.api.model.ai.vo.ChatConversationDetailVO;
import top.harrylei.community.api.model.ai.vo.ChatConversationVO;
import top.harrylei.community.service.ai.repository.entity.ChatConversationDO;

/**
 * 对话数据转换器
 *
 * @author harry
 */
@Mapper(componentModel = "spring")
public interface ChatConversationStructMapper {

    /**
     * DO转DTO
     */
    ChatConversationDTO toDTO(ChatConversationDO aiConversationDO);

    /**
     * DTO转VO
     */
    ChatConversationVO toVO(ChatConversationDTO chatConversationDTO);

    /**
     * DTO转详情VO
     */
    @Mapping(target = "messages", ignore = true)
    ChatConversationDetailVO toDetailVO(ChatConversationDTO chatConversationDTO);
}