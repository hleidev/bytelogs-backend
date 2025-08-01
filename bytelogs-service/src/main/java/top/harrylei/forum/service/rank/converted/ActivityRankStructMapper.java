package top.harrylei.forum.service.rank.converted;

import org.mapstruct.Mapper;
import top.harrylei.forum.api.model.rank.dto.ActivityRankDTO;
import top.harrylei.forum.api.model.rank.vo.ActivityRankVO;

/**
 * 排行榜结构映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring")
public interface ActivityRankStructMapper {

    ActivityRankVO toVO(ActivityRankDTO activityRankDTO);
}