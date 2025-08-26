package top.harrylei.forum.service.ai.converted;

import org.mapstruct.Mapper;
import top.harrylei.forum.api.model.ai.dto.AIMessageDTO;
import top.harrylei.forum.api.model.ai.vo.AIMessageVO;
import top.harrylei.forum.service.ai.repository.entity.AIMessageDO;

/**
 * 消息数据转换器
 *
 * @author harry
 */
@Mapper(componentModel = "spring")
public interface AIMessageStructMapper {

    /**
     * DO转DTO
     */
    AIMessageDTO toDTO(AIMessageDO aiMessageDO);

    /**
     * DTO转VO
     */
    AIMessageVO toVO(AIMessageDTO aiMessageDTO);
}