package top.harrylei.forum.service.user.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import top.harrylei.forum.api.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.user.dto.UserInfoDetailDTO;
import top.harrylei.forum.api.model.user.dto.UserDetailDTO;
import top.harrylei.forum.api.model.user.req.UserInfoReq;
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
    UserInfoDetailDTO toDTO(UserInfoDO userInfo);

    /**
     * 将数据传输对象转换为视图对象
     *
     * @param userInfoDetailDTO 用户信息DTO
     * @return 用户信息视图对象
     */
    UserInfoVO toVO(UserInfoDetailDTO userInfoDetailDTO);

    /**
     * 将请求对象中的数据更新到DTO对象
     * <p>
     * 只更新非空字段，空字符串也视为空值不更新
     * </p>
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
    void updateDTOFromReq(UserInfoReq req, @MappingTarget UserInfoDetailDTO dto);

    /**
     * 将数据传输对象转化为数据库实体对象
     *
     * @param userInfoDTO 数据传输对象
     * @return 数据库实体对象
     */
    @Mapping(target = "userRole", source = "role", qualifiedByName = "roleNameToCode")
    UserInfoDO toDO(UserInfoDetailDTO userInfoDTO);

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
     * 将角色代码转换为角色名称（枚举名）
     *
     * @param code 角色代码
     * @return 角色名称
     */
    @Named("codeToRoleName")
    static String codeToRoleName(Integer code) {
        return UserRoleEnum.getNameByCode(code);
    }

    /**
     * 将角色名称转换为角色代码
     *
     * @param roleName 角色名称
     * @return 角色代码
     */
    @Named("roleNameToCode")
    static Integer roleNameToCode(String roleName) {
        return UserRoleEnum.getCodeByName(roleName);
    }

    /**
     * 将角色代码转换为角色文本显示值
     *
     * @param code 角色代码
     * @return 角色文本
     */
    @Named("codeToRoleText")
    default String codeToRoleText(Integer code) {
        return UserRoleEnum.getLabelByCode(code);
    }

    /**
     * 将状态码转换为状态文本
     *
     * @param status 状态码
     * @return 状态文本
     */
    @Named("statusToText")
    default String statusToText(Integer status) {
        return status != null && status == 1 ? "启用" : "禁用";
    }

    /**
     * 将删除标记转换为删除状态文本
     *
     * @param deleted 删除标记
     * @return 删除状态文本
     */
    @Named("deletedToText")
    default String deletedToText(Integer deleted) {
        return deleted != null && deleted == 1 ? "已删除" : "未删除";
    }
}
