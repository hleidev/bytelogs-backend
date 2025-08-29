package top.harrylei.community.api.model.statistics.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.community.api.model.base.BaseDTO;

/**
 * 文章统计数据传输对象
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ArticleStatisticsDTO extends BaseDTO {

    /**
     * 阅读次数
     */
    private Long readCount;

    /**
     * 点赞次数
     */
    private Long praiseCount;

    /**
     * 收藏次数
     */
    private Long collectCount;

    /**
     * 评论次数
     */
    private Long commentCount;

    public ArticleStatisticsDTO() {
        this.readCount = 0L;
        this.praiseCount = 0L;
        this.collectCount = 0L;
        this.commentCount = 0L;
    }
}