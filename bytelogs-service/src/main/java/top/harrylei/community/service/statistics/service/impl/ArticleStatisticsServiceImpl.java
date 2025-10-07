package top.harrylei.community.service.statistics.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.community.api.model.statistics.dto.ArticleStatisticsDTO;
import top.harrylei.community.service.statistics.converted.ArticleStatisticsStructMapper;
import top.harrylei.community.service.statistics.repository.dao.ArticleStatisticsDAO;
import top.harrylei.community.service.statistics.repository.entity.ArticleStatisticsDO;
import top.harrylei.community.service.statistics.service.ArticleStatisticsService;

/**
 * 文章统计服务实现类
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleStatisticsServiceImpl implements ArticleStatisticsService {

    private final ArticleStatisticsDAO articleStatisticsDAO;
    private final ArticleStatisticsStructMapper articleStatisticsStructMapper;

    @Override
    public ArticleStatisticsDTO getArticleStatistics(Long articleId) {
        if (articleId == null) {
            return new ArticleStatisticsDTO();
        }
        ArticleStatisticsDO statistics = articleStatisticsDAO.getByArticleId(articleId);
        if (statistics == null) {
            return new ArticleStatisticsDTO();
        }
        return articleStatisticsStructMapper.toDTO(statistics);
    }

}