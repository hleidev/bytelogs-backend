package top.harrylei.forum.service.ai.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.harrylei.forum.api.model.ai.dto.AIConversationDTO;
import top.harrylei.forum.api.model.ai.vo.AIConversationDetailVO;
import top.harrylei.forum.api.model.ai.vo.AIConversationVO;
import top.harrylei.forum.service.ai.repository.entity.AIConversationDO;

/**
 * 对话数据转换器
 *
 * @author harry
 */
@Mapper(componentModel = "spring")
public interface AIConversationStructMapper {

    /**
     * DO转DTO
     */
    AIConversationDTO toDTO(AIConversationDO aiConversationDO);

    /**
     * DTO转VO
     */
    AIConversationVO toVO(AIConversationDTO aiConversationDTO);

    /**
     * DTO转详情VO
     */
    @Mapping(target = "messages", ignore = true)
    AIConversationDetailVO toDetailVO(AIConversationDTO aiConversationDTO);
}