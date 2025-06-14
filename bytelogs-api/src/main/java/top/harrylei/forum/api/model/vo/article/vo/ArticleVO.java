package top.harrylei.forum.api.model.vo.article.vo;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.forum.api.model.entity.BaseVO;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.vo.article.dto.ArticleDetailDTO;

/**
 * 文章详情视图对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleVO extends BaseVO {

    /**
     * 文章ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文章类型：1-博文，2-问答
     */
    private Integer articleType;

    /**
     * 文章类型：1-博文，2-问答
     */
    private String articleTypeLabel;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 短标题
     */
    private String shortTitle;

    /**
     * 文章头图
     */
    private String picture;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 文章分类
     */
    private CategorySimpleVO category;

    /**
     * 标签列表
     */
    private List<TagSimpleVO> tags;

    /**
     * 文章来源
     */
    private Integer source;

    /**
     * 文章来源
     */
    private String sourceLabel;

    /**
     * 原文链接
     */
    private String sourceUrl;

    /**
     * 是否官方：0-非官方，1-官方
     */
    private Integer official;

    /**
     * 是否官方：0-非官方，1-官方
     */
    private String officialLabel;

    /**
     * 是否置顶：0-不置顶，1-置顶
     */
    private YesOrNoEnum topping;

    /**
     * 是否置顶：0-不置顶，1-置顶
     */
    private String toppingLabel;

    /**
     * 是否加精：0-不加精，1-加精
     */
    private Integer cream;

    /**
     * 是否加精：0-不加精，1-加精
     */
    private String creamLabel;

    /**
     * 文章状态：草稿、审核、已发布、下架、驳回
     */
    private Integer status;

    /**
     * 文章状态：草稿、审核、已发布、下架、驳回
     */
    private String statusLabel;

    /**
     * 当前发布版本号
     */
    private Integer currentVersion;

    /**
     * 文章内容（当前主版本）
     */
    private String content;

    /**
     * 历史版本列表（如详情页展示可选）
     */
    private List<ArticleDetailDTO> versions;

    /**
     * 是否删除
     */
    private Integer deleted;

    /**
     * 是否删除
     */
    private String deletedLabel;
}