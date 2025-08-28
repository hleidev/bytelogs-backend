package top.harrylei.community.api.model.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import top.harrylei.community.api.model.statistics.StatisticsVO;
import top.harrylei.community.api.model.user.vo.UserInfoVO;

/**
 * 文章详情对象
 *
 * @author harry
 */
@Data
@Schema(description = "文章详情对象")
@Accessors(chain = true)
public class ArticleDetailVO {

    /**
     * 文章信息
     */
    private ArticleVO article;

    /**
     * 作者信息
     */
    private UserInfoVO author;

    /**
     * 统计信息
     */
    private StatisticsVO statistics;
}