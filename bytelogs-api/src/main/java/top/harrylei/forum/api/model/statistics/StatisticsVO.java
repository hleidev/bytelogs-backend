package top.harrylei.forum.api.model.statistics;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统计信息视图对象
 *
 * @author harry
 */
@Data
@Accessors(chain = true)
public class StatisticsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 阅读量
     */
    private Long readCount;

    /**
     * 点赞数
     */
    private Long praiseCount;

    /**
     * 收藏数
     */
    private Long collectionCount;
}