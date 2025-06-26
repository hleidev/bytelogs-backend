package top.harrylei.forum.service.user.converted;

import org.mapstruct.Mapper;
import top.harrylei.forum.core.common.converter.EnumConverter;

/**
 * 用户足迹结构映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring", uses = {EnumConverter.class})
public interface UserFootStructMapper {
}