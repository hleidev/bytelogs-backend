package top.harrylei.forum.api.model.vo.article.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.entity.BaseDTO;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.article.ArticleSourceEnum;
import top.harrylei.forum.api.model.enums.article.ArticleStatusEnum;
import top.harrylei.forum.api.model.enums.article.ArticleTypeEnum;

/**
 * 文章详情传输对象
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
    private CategoryDTO categoryId;

    /**
     * 标签列表
     */
    private List<TagDTO> tags;

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
    private YesOrNoEnum official;

    /**
     * 置顶标记
     */
    private YesOrNoEnum topping;

    /**
     * 加精标记
     */
    private YesOrNoEnum cream;

    /**
     * 状态：0-草稿，1-待审核，2-已发布，3-下架，4-驳回
     */
    private ArticleStatusEnum status;

    /**
     * 当前发布版本号
     */
    private Integer currentVersion;

    /**
     * 文章内容（详情专有）
     */
    private String content;

    /**
     * 是否删除
     */
    private YesOrNoEnum deleted;
}