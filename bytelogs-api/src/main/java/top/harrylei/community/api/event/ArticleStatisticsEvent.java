package top.harrylei.community.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import top.harrylei.community.api.enums.article.ArticleStatisticsEnum;

import java.io.Serial;

/**
 * 文章统计事件模型
 *
 * @author harry
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArticleStatisticsEvent extends BaseEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 操作用户ID（可选，阅读量统计时使用）
     */
    private Long userId;

    /**
     * 统计操作类型
     */
    private ArticleStatisticsEnum actionType;
}