package top.harrylei.forum.service.user.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import top.harrylei.forum.api.model.enums.CollectionStatusEnum;
import top.harrylei.forum.api.model.enums.PraiseStatusEnum;
import top.harrylei.forum.api.model.enums.ReadStatusEnum;
import top.harrylei.forum.api.model.enums.comment.CommentStatusEnum;
import top.harrylei.forum.api.model.enums.comment.ContentTypeEnum;
import top.harrylei.forum.api.model.vo.user.dto.UserFootDTO;
import top.harrylei.forum.core.common.converter.EnumConverter;
import top.harrylei.forum.service.user.repository.entity.UserFootDO;

/**
 * 用户足迹结构映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring", uses = {EnumConverter.class})
public interface UserFootStructMapper {

    @Mapping(target = "contentType", source = "contentType", qualifiedByName = "CodeToContentTypeEnum")
    @Mapping(target = "deleted", source = "deleted", qualifiedByName = "CodeToYesOrNoEnum")
    @Mapping(target = "collectionState", source = "collectionState", qualifiedByName = "CodeToCollectionStatusEnum")
    @Mapping(target = "readState", source = "readState", qualifiedByName = "CodeToReadStatusEnum")
    @Mapping(target = "commentState", source = "commentState", qualifiedByName = "CodeToCommentStatusEnum")
    @Mapping(target = "praiseState", source = "praiseState", qualifiedByName = "CodeToPraiseStatusEnum")
    UserFootDTO toDTO(UserFootDO userFoot);

    @Named("CodeToContentTypeEnum")
    default ContentTypeEnum codeToContentTypeEnum(Integer code) {
        return code != null ? ContentTypeEnum.fromCode(code) : null;
    }

    @Named("CodeToCollectionStatusEnum")
    default CollectionStatusEnum codeToCollectionStatusEnum(Integer code) {
        return code != null ? CollectionStatusEnum.fromCode(code) : null;
    }

    @Named("CodeToReadStatusEnum")
    default ReadStatusEnum codeToReadStatusEnum(Integer code) {
        return code != null ? ReadStatusEnum.fromCode(code) : null;
    }

    @Named("CodeToCommentStatusEnum")
    default CommentStatusEnum codeToCommentStatusEnum(Integer code) {
        return code != null ? CommentStatusEnum.fromCode(code) : null;
    }

    @Named("CodeToPraiseStatusEnum")
    default PraiseStatusEnum codeToPraiseStatusEnum(Integer code) {
        return code != null ? PraiseStatusEnum.fromCode(code) : null;
    }
}