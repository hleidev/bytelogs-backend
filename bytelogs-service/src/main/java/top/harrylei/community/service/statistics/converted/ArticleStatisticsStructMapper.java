package top.harrylei.community.service.statistics.converted;

import org.mapstruct.Mapper;
import top.harrylei.community.api.model.statistics.ArticleStatisticsVO;
import top.harrylei.community.api.model.statistics.dto.ArticleStatisticsDTO;
import top.harrylei.community.service.statistics.repository.entity.ArticleStatisticsDO;

/**
 * 文章统计结构映射器
 *
 * @author harry
 */
@Mapper(componentModel = "spring")
public interface ArticleStatisticsStructMapper {

    /**
     * DTO转VO
     *
     * @param dto 文章统计DTO
     * @return 统计VO
     */
    ArticleStatisticsVO toVO(ArticleStatisticsDTO dto);

    ArticleStatisticsDTO toDTO(ArticleStatisticsDO statistics);
}