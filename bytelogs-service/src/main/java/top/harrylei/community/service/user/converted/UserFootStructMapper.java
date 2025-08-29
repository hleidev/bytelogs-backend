package top.harrylei.community.service.user.converted;

import org.mapstruct.Mapper;
import top.harrylei.community.api.model.user.dto.UserFootDTO;
import top.harrylei.community.service.user.repository.entity.UserFootDO;

/**
 * 用户足迹结构映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring")
public interface UserFootStructMapper {

    UserFootDTO toDTO(UserFootDO userFoot);
}