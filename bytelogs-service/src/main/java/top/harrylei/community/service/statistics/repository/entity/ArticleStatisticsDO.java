package top.harrylei.community.service.statistics.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.community.api.model.base.BaseDO;

import java.io.Serial;

/**
 * 文章统计实体对象
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_statistics")
@Accessors(chain = true)
public class ReadCountDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 阅读次数
     */
    private Integer readCount;

    /**
     * 点赞次数
     */
    private Integer praiseCount;

    /**
     * 收藏次数
     */
    private Integer collectCount;

    /**
     * 评论次数
     */
    private Integer commentCount;
}