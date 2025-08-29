package top.harrylei.community.service.user.converted;

import org.mapstruct.Mapper;
import top.harrylei.community.api.model.user.dto.UserFollowDTO;
import top.harrylei.community.service.user.repository.entity.UserFollowDO;

/**
 * 用户关注对象转换映射器
 * <p>
 * 负责用户关注相关DO、DTO、VO之间的转换
 * </p>
 *
 * @author harry
 */
@Mapper(componentModel = "spring")
public interface UserFollowStructMapper {

    /**
     * 将数据库实体对象转换为数据传输对象
     *
     * @param userFollowDO 用户关注数据库实体
     * @return 用户关注DTO
     */
    UserFollowDTO toDTO(UserFollowDO userFollowDO);

    /**
     * 将数据传输对象转换为数据库实体对象
     *
     * @param userFollowDTO 用户关注DTO
     * @return 用户关注数据库实体
     */
    UserFollowDO toDO(UserFollowDTO userFollowDTO);
}