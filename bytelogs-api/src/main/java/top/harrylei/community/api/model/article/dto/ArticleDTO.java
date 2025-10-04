package top.harrylei.community.api.model.article.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.community.api.model.base.BaseDTO;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.article.ArticleSourceEnum;
import top.harrylei.community.api.enums.article.ArticleTypeEnum;
import top.harrylei.community.api.enums.article.CreamStatusEnum;
import top.harrylei.community.api.enums.article.OfficialStatusEnum;
import top.harrylei.community.api.enums.article.ArticlePublishStatusEnum;
import top.harrylei.community.api.enums.article.ToppingStatusEnum;

import java.util.List;

/**
 * 文章详情传输对象
 *
 * @author harry
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleDTO extends BaseDTO {

    /**
     * 文章标题
     */
    private String title;

    /**
     * 短标题
     */
    private String shortTitle;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 文章类型：1-博文，2-问答
     */
    private ArticleTypeEnum articleType;

    /**
     * 文章头图
     */
    private String picture;

    /**
     * 类目ID
     */
    private Long categoryId;

    /**
     * 文章分类
     */
    private CategorySimpleDTO category;

    /**
     * 标签列表
     */
    private List<Long> tagIds;

    /**
     * 标签列表
     */
    private List<TagSimpleDTO> tags;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 来源：1-转载，2-原创，3-翻译
     */
    private ArticleSourceEnum source;

    /**
     * 原文链接
     */
    private String sourceUrl;

    /**
     * 官方标记
     */
    private OfficialStatusEnum official;

    /**
     * 置顶标记
     */
    private ToppingStatusEnum topping;

    /**
     * 加精标记
     */
    private CreamStatusEnum cream;

    /**
     * 状态：0-未发布，1-已发布
     */
    private ArticlePublishStatusEnum status;

    /**
     * 版本总数
     */
    private Integer versionCount;

    /**
     * 文章内容（详情专有）
     */
    private String content;

    /**
     * 是否删除
     */
    private DeleteStatusEnum deleted;

}