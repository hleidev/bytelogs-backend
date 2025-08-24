package top.harrylei.forum.service.user.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import top.harrylei.forum.api.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.user.dto.UserInfoDTO;
import top.harrylei.forum.api.model.user.dto.UserDetailDTO;
import top.harrylei.forum.api.model.user.req.UserInfoUpdateReq;
import top.harrylei.forum.api.model.user.vo.UserDetailVO;
import top.harrylei.forum.api.model.user.vo.UserInfoVO;
import top.harrylei.forum.api.model.user.vo.UserListItemVO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;

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
    @Mapping(source = "userRole", target = "role", qualifiedByName = "codeToRoleName")
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
    @Mapping(target = "role", ignore = true)
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
    @Mapping(target = "userRole", source = "role", qualifiedByName = "roleNameToCode")
    UserInfoDO toDO(UserInfoDTO userInfoDTO);

    /**
     * 将完整用户DTO转换为用户列表项视图对象
     *
     * @param userDetailDTO 完整用户DTO
     * @return 用户列表项视图对象
     */
    @Mapping(source = "userRole", target = "role", qualifiedByName = "codeToRoleText")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToText")
    @Mapping(source = "deleted", target = "deleted", qualifiedByName = "deletedToText")
    UserListItemVO toUserListItemVO(UserDetailDTO userDetailDTO);

    /**
     * 将完整用户DTO转换为用户详情视图对象
     *
     * @param userDetailDTO 完整用户DTO
     * @return 用户详情视图对象
     */
    @Mapping(source = "userRole", target = "role", qualifiedByName = "codeToRoleText")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToText")
    @Mapping(source = "deleted", target = "deleted", qualifiedByName = "deletedToText")
    UserDetailVO toUserDetailVO(UserDetailDTO userDetailDTO);

    /**
     * Integer到UserRoleEnum的自动映射
     */
    default UserRoleEnum mapUserRole(Integer code) {
        return UserRoleEnum.fromCode(code);
    }

    /**
     * UserRoleEnum到Integer的自动映射
     */
    default Integer mapUserRole(UserRoleEnum role) {
        return role != null ? role.getCode() : null;
    }

    /**
     * 角色代码转角色名称
     */
    @Named("codeToRoleName")
    default String codeToRoleName(Integer code) {
        UserRoleEnum role = UserRoleEnum.fromCode(code);
        return role != null ? role.name() : UserRoleEnum.NORMAL.name();
    }

    /**
     * 角色名称转角色代码
     */
    @Named("roleNameToCode")
    default Integer roleNameToCode(String roleName) {
        UserRoleEnum role = UserRoleEnum.fromName(roleName);
        return role != null ? role.getCode() : UserRoleEnum.NORMAL.getCode();
    }

    /**
     * 角色代码转显示文本
     */
    @Named("codeToRoleText")
    default String codeToRoleText(Integer code) {
        return UserRoleEnum.getLabelByCode(code);
    }

    /**
     * 状态码转显示文本
     */
    @Named("statusToText")
    default String statusToText(Integer status) {
        return status != null && status == 1 ? "启用" : "禁用";
    }

    /**
     * 删除标记转显示文本
     */
    @Named("deletedToText")
    default String deletedToText(Integer deleted) {
        return deleted != null && deleted == 1 ? "已删除" : "未删除";
    }
}
