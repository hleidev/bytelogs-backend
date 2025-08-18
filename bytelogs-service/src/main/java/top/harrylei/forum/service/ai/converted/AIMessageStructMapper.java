package top.harrylei.forum.service.ai.converted;

import org.mapstruct.Mapper;
import top.harrylei.forum.api.model.ai.dto.AIMessageDTO;
import top.harrylei.forum.api.model.ai.vo.AIMessageVO;
import top.harrylei.forum.service.ai.repository.entity.AIMessageDO;

import java.util.List;

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
     * DO列表转DTO列表
     */
    List<AIMessageDTO> toDTOList(List<AIMessageDO> aiMessageDOList);

    /**
     * DTO转VO
     */
    AIMessageVO toVO(AIMessageDTO aiMessageDTO);

    /**
     * DTO列表转VO列表
     */
    List<AIMessageVO> toVOList(List<AIMessageDTO> aiMessageDTOList);
}