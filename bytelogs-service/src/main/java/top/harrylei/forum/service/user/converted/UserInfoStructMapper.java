package top.harrylei.forum.service.user.converted;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import top.harrylei.forum.api.model.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.api.model.vo.user.req.UserInfoReq;
import top.harrylei.forum.api.model.vo.user.vo.UserInfoVO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;

/**
 * 用户信息对象转换映射器 负责DO、DTO、VO之间的转换
 */
@Mapper(componentModel = "spring", imports = {StringUtils.class})
public interface UserInfoStructMapper {

    /**
     * 将数据库实体对象转换为数据传输对象
     * 
     * @param userInfo 用户信息数据库实体
     * @return 用户信息DTO
     */
    @Mapping(source = "userRole", target = "role", qualifiedByName = "userRoleMapRole")
    BaseUserInfoDTO toDTO(UserInfoDO userInfo);

    /**
     * 将数字角色代码映射为字符串角色名称
     *
     * @param role 角色代码（1-管理员，其他-普通用户）
     * @return 角色名称
     */
    @Named("userRoleMapRole")
    static String userRoleMapRole(Integer role) {
        if (Objects.equals(role, UserRoleEnum.ADMIN.getCode())) {
            return "ADMIN";
        }
        return "NORMAL";
    }

    /**
     * 将数据传输对象转换为视图对象
     *
     * @param baseUserInfoDTO 用户信息DTO
     * @return 用户信息视图对象
     */
    UserInfoVO toVO(BaseUserInfoDTO baseUserInfoDTO);

    /**
     * 将请求对象中的数据更新到DTO对象
     * 只更新非空字段，空字符串也视为空值不更新
     *
     * @param req 用户信息请求对象
     * @param dto 目标用户信息DTO对象
     */
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "extend", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void updateDTOFromReq(UserInfoReq req, @MappingTarget BaseUserInfoDTO dto);

    /**
     * 将数据传输对象转化为数据库实体对象
     *
     * @param userInfoDTO 数据传输对象
     * @return 数据库实体对象
     */
    @Mapping(target = "userRole", source = "role", qualifiedByName = "roleMapUserRole")
    UserInfoDO toDO(BaseUserInfoDTO userInfoDTO);

    @Named("roleMapUserRole")
    static Integer roleMapUserRole(String role) {
        if (Objects.equals(role, UserRoleEnum.ADMIN.getDesc())) {
            return 1;
        }
        return 0;
    }
}
