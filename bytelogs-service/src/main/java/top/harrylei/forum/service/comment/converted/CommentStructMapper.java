package top.harrylei.forum.service.comment.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import top.harrylei.forum.api.model.comment.dto.CommentDTO;
import top.harrylei.forum.api.model.comment.req.CommentSaveReq;
import top.harrylei.forum.api.model.comment.vo.CommentMyVO;
import top.harrylei.forum.api.model.comment.vo.CommentVO;
import top.harrylei.forum.api.model.comment.vo.SubCommentVO;
import top.harrylei.forum.api.model.comment.vo.TopCommentVO;
import top.harrylei.forum.core.common.converter.EnumConverter;
import top.harrylei.forum.service.comment.repository.entity.CommentDO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论结构映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring", uses = {EnumConverter.class})
public interface CommentStructMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "topCommentId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    CommentDTO toDTO(CommentSaveReq req);

    CommentDO toDO(CommentDTO dto);

    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "userAvatar", ignore = true)
    @Mapping(target = "praised", ignore = true)
    @Mapping(target = "praiseCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "childComments", ignore = true)
    TopCommentVO toTopVO(CommentDO commentDO);

    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "userAvatar", ignore = true)
    @Mapping(target = "praised", ignore = true)
    @Mapping(target = "praiseCount", ignore = true)
    @Mapping(target = "parentContent", ignore = true)
    SubCommentVO toSubVO(CommentDO commentDO);

    @Mapping(target = "articleTitle", ignore = true)
    @Mapping(target = "parentContent", ignore = true)
    CommentMyVO toMyVO(CommentDO commentDO);

    @Mapping(target = "topCommentId", ignore = true)
    @Mapping(target = "parentCommentId", ignore = true)
    @Mapping(target = "childComments", source = "childComments", qualifiedByName = "SubCommentListToVOList")
    CommentVO toVO(TopCommentVO topCommentVO);

    @Mapping(target = "topCommentId", ignore = true)
    @Mapping(target = "parentCommentId", ignore = true)
    @Mapping(target = "childComments", ignore = true)
    CommentVO toVO(SubCommentVO subCommentVO);

    @Named("SubCommentListToVOList")
    default List<CommentVO> subCommentListToVOList(List<SubCommentVO> subComments) {
        if (subComments == null) {
            return null;
        }
        return subComments.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
}