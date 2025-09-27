package top.harrylei.community.api.model.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.harrylei.community.api.enums.article.*;
import top.harrylei.community.api.model.base.BaseVO;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.article.ArticlePublishStatusEnum;

import java.util.List;

/**
 * 文章详情对象
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "文章对象")
public class ArticleVO extends BaseVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 文章类型：1-博文，2-问答
     */
    @Schema(description = "文章类型：1-博文，2-问答", example = "{\"code\":1,\"label\":\"博文\"}")
    private ArticleTypeEnum articleType;

    /**
     * 文章标题
     */
    @Schema(description = "文章标题", example = "Spring 事务全解析")
    private String title;

    /**
     * 短标题
     */
    @Schema(description = "短标题", example = "Spring事务")
    private String shortTitle;

    /**
     * 文章头图
     */
    @Schema(description = "文章头图", example = "https://example.com/article.jpg")
    private String picture;

    /**
     * 文章摘要
     */
    @Schema(description = "文章摘要", example = "本文详细讲解了Spring事务的实现原理。")
    private String summary;

    /**
     * 文章分类ID
     */
    @Schema(description = "文章分类ID", example = "1")
    private Long categoryId;

    /**
     * 标签ID列表
     */
    @Schema(description = "标签ID列表", example = "[1, 2, 3]")
    private List<Long> tagIds;

    /**
     * 文章来源：1-转载，2-原创，3-翻译
     */
    @Schema(description = "文章来源：1-转载，2-原创，3-翻译", example = "{\"code\":2,\"label\":\"原创\"}")
    private ArticleSourceEnum source;

    /**
     * 原文链接
     */
    @Schema(description = "原文链接", example = "https://juejin.cn/post/123")
    private String sourceUrl;

    /**
     * 是否官方：0-非官方，1-官方
     */
    @Schema(description = "是否官方：0-非官方，1-官方", example = "{\"code\":1,\"label\":\"官方\"}")
    private OfficialStatusEnum official;

    /**
     * 是否置顶：0-不置顶，1-置顶
     */
    @Schema(description = "是否置顶：0-不置顶，1-置顶", example = "{\"code\":1,\"label\":\"置顶\"}")
    private ToppingStatusEnum topping;

    /**
     * 是否加精：0-不加精，1-加精
     */
    @Schema(description = "是否加精：0-不加精，1-加精", example = "{\"code\":1,\"label\":\"加精\"}")
    private CreamStatusEnum cream;

    /**
     * 文章状态：0-未发布，1-已发布，2-待审核
     */
    @Schema(description = "文章状态：0-未发布，1-已发布，2-待审核", example = "{\"code\":2,\"label\":\"已发布\"}")
    private ArticlePublishStatusEnum status;

    /**
     * 版本总数
     */
    @Schema(description = "版本总数", example = "5")
    private Integer versionCount;

    /**
     * 文章内容
     */
    @Schema(description = "文章内容", example = "本文介绍了Spring事务管理的底层实现机制……")
    private String content;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Schema(description = "是否删除：0-未删除，1-已删除", example = "{\"code\":0,\"label\":\"未删除\"}")
    private DeleteStatusEnum deleted;
}