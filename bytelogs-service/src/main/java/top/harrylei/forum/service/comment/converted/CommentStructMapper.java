package top.harrylei.forum.service.comment.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.harrylei.forum.api.model.vo.comment.dto.CommentDTO;
import top.harrylei.forum.api.model.vo.comment.req.CommentSaveReq;
import top.harrylei.forum.core.common.converter.EnumConverter;
import top.harrylei.forum.service.comment.repository.entity.CommentDO;

/**
 * 评论结构映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring", uses = {EnumConverter.class})
public interface CommentStructMapper {

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    CommentDTO toDTO(CommentSaveReq req);

    CommentDO toDO(CommentDTO dto);
}