package top.harrylei.community.service.user.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import top.harrylei.community.api.model.user.dto.UserDetailDTO;
import top.harrylei.community.api.model.user.dto.UserInfoDTO;
import top.harrylei.community.api.model.user.req.UserInfoUpdateReq;
import top.harrylei.community.api.model.user.vo.UserDetailVO;
import top.harrylei.community.api.model.user.vo.UserInfoVO;
import top.harrylei.community.api.model.user.vo.UserListItemVO;
import top.harrylei.community.service.user.repository.entity.UserInfoDO;

/**
 * 用户对象转换映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring")
public interface UserStructMapper {

    /**
     * 将数据库实体对象转换为数据传输对象
     *
     * @param userInfo 用户信息数据库实体
     * @return 用户信息DTO
     */
    UserInfoDTO toDTO(UserInfoDO userInfo);

    /**
     * 将数据传输对象转换为视图对象
     *
     * @param userInfoDTO 用户信息DTO
     * @return 用户信息视图对象
     */
    UserInfoVO toVO(UserInfoDTO userInfoDTO);

    /**
     * 将请求对象中的数据更新到DTO对象
     *
     * @param req 用户信息请求对象
     * @param dto 目标用户信息DTO对象
     */
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "userRole", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "extend", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    void applyUserInfoUpdates(UserInfoUpdateReq req, @MappingTarget UserInfoDTO dto);

    /**
     * 将数据传输对象转化为数据库实体对象
     *
     * @param userInfoDTO 数据传输对象
     * @return 数据库实体对象
     */
    UserInfoDO toDO(UserInfoDTO userInfoDTO);

    /**
     * 将完整用户DTO转换为用户列表项视图对象
     *
     * @param userDetailDTO 完整用户DTO
     * @return 用户列表项视图对象
     */
    UserListItemVO toUserListItemVO(UserDetailDTO userDetailDTO);

    /**
     * 将完整用户DTO转换为用户详情视图对象
     *
     * @param userDetailDTO 完整用户DTO
     * @return 用户详情视图对象
     */
    UserDetailVO toUserDetailVO(UserDetailDTO userDetailDTO);
}
