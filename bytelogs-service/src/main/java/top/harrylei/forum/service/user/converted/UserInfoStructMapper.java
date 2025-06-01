package top.harrylei.forum.service.user.converted;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import top.harrylei.forum.api.model.enums.user.UserRoleEnum;
import top.harrylei.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import top.harrylei.forum.api.model.vo.user.vo.UserInfoVO;
import top.harrylei.forum.service.user.repository.entity.UserInfoDO;

import java.util.Objects;

/**
 * 用户信息对象转换映射器
 * 负责DO、DTO、VO之间的转换
 */
@Mapper(componentModel = "spring")
public interface UserInfoStructMapper {

    /**
     * 将数据库实体对象转换为数据传输对象
     * 
     * @param userInfo 用户信息数据库实体
     * @return 用户信息DTO
     */
    @Mapping(source = "userRole", target = "role", qualifiedByName = "mapRole")
    BaseUserInfoDTO toDTO(UserInfoDO userInfo);

    /**
     * 将数字角色代码映射为字符串角色名称
     * 
     * @param role 角色代码（1-管理员，其他-普通用户）
     * @return 角色名称
     */
    @Named("mapRole")
    static String mapRole(Integer role) {
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
}
