package top.harrylei.forum.service.user.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import top.harrylei.forum.api.model.enums.user.UserFollowStatusEnum;
import top.harrylei.forum.api.model.vo.user.dto.UserFollowDTO;
import top.harrylei.forum.core.common.converter.EnumConverter;
import top.harrylei.forum.service.user.repository.entity.UserFollowDO;

/**
 * 用户关注对象转换映射器
 * <p>
 * 负责用户关注相关DO、DTO、VO之间的转换
 * </p>
 *
 * @author harry
 */
@Mapper(componentModel = "spring", uses = {EnumConverter.class})
public interface UserFollowStructMapper {

    /**
     * 将数据库实体对象转换为数据传输对象
     *
     * @param userFollowDO 用户关注数据库实体
     * @return 用户关注DTO
     */
    @Mapping(source = "followState", target = "followState", qualifiedByName = "CodeToStatusEnum")
    @Mapping(source = "deleted", target = "deleted", qualifiedByName = "CodeToYesOrNoEnum")
    UserFollowDTO toDTO(UserFollowDO userFollowDO);

    /**
     * 将数据传输对象转换为数据库实体对象
     *
     * @param userFollowDTO 用户关注DTO
     * @return 用户关注数据库实体
     */
    @Mapping(source = "followState", target = "followState", qualifiedByName = "StatusEnumToCode")
    @Mapping(source = "deleted", target = "deleted", qualifiedByName = "YesOrNoEnumToCode")
    UserFollowDO toDO(UserFollowDTO userFollowDTO);

    /**
     * 将关注状态代码转换为枚举
     *
     * @param code 状态代码
     * @return 关注状态枚举
     */
    @Named("CodeToStatusEnum")
    static UserFollowStatusEnum codeToStatusEnum(Integer code) {
        return UserFollowStatusEnum.fromCode(code);
    }

    /**
     * 将关注状态枚举转换为代码
     *
     * @param status 关注状态枚举
     * @return 状态代码
     */
    @Named("StatusEnumToCode")
    static Integer statusEnumToCode(UserFollowStatusEnum status) {
        return status != null ? status.getCode() : null;
    }
}