package top.harrylei.forum.api.model.vo.article.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.entity.BaseDTO;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;

/**
 * 文章历史版本/内容详情传输对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleDetailDTO extends BaseDTO {

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 是否为最新版本：1-是，0-否
     */
    private Integer latest;

    /**
     * 是否为发布版本：1-是，0-否
     */
    private PublishStatusEnum published;

    /**
     * 编辑操作令牌（防止并发编辑）
     */
    private String editToken;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private YesOrNoEnum deleted;
}