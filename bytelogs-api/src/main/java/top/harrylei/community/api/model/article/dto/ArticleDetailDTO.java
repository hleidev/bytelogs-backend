package top.harrylei.community.api.model.article.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.community.api.model.base.BaseDTO;
import top.harrylei.community.api.enums.YesOrNoEnum;

/**
 * 文章历史版本/内容详情传输对象
 *
 * @author harry
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
     * 是否删除：0-未删除，1-已删除
     */
    private YesOrNoEnum deleted;
}