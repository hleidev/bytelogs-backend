package top.harrylei.community.service.statistics.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.community.api.model.base.BaseDO;

import java.io.Serial;

/**
 * 内容访问计数实体对象
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("read_count")
@Accessors(chain = true)
public class ReadCountDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 内容类型：1-文章，2-评论
     */
    private Integer contentType;

    /**
     * 访问计数
     */
    private Integer cnt;
}