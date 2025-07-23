package top.harrylei.forum.api.model.vo.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import top.harrylei.forum.api.validation.SecureContent.ContentSecurityType;
import top.harrylei.forum.api.model.enums.article.ArticleSourceEnum;
import top.harrylei.forum.api.model.enums.article.ArticleTypeEnum;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.validation.SecureContent;

import java.util.List;

/**
 * 用户发布文章请求参数
 *
 * @author harry
 */
@Data
@Schema(description = "文章发布请求")
public class ArticlePostReq {

    /**
     * 文章标题
     */
    @NotBlank(message = "文章标题不能为空")
    @Size(min = 2, max = 200, message = "文章标题长度为2-200个字符")
    @SecureContent(contentType = ContentSecurityType.PLAIN_TEXT, allowEmpty = false)
    @Schema(description = "文章标题", example = "深入理解Spring事务", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    /**
     * 短标题
     */
    @Size(max = 200, message = "短标题长度不能超过200个字符")
    @SecureContent(contentType = ContentSecurityType.PLAIN_TEXT)
    @Schema(description = "短标题", example = "Spring事务", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String shortTitle;

    /**
     * 文章头图
     */
    @Size(max = 512, message = "文章头图链接长度不能超过512个字符")
    @Schema(description = "文章头图", example = "https://oss.aliyun.com/image.jpg", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String picture;

    /**
     * 文章摘要
     */
    @Size(max = 512, message = "摘要长度不能超过512个字符")
    @SecureContent(contentType = ContentSecurityType.PLAIN_TEXT)
    @Schema(description = "文章摘要", example = "本文深入讲解了Spring事务的实现机制", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String summary;

    /**
     * 文章类型：1-博文，2-问答
     */
    @NotNull(message = "文章类型不能为空")
    @Schema(description = "文章类型，1-博文，2-问答", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private ArticleTypeEnum articleType;

    /**
     * 类目ID
     */
    @NotNull(message = "类目ID不能为空")
    @Schema(description = "类目ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    /**
     * 标签ID列表
     */
    @Size(max = 5, message = "最多选择5个标签")
    @Schema(description = "标签ID列表", example = "[1,2,3]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<Long> tagIds;

    /**
     * 文章来源：1-转载，2-原创，3-翻译
     */
    @NotNull(message = "文章来源不能为空")
    @Schema(description = "文章来源，1-转载，2-原创，3-翻译", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private ArticleSourceEnum source;

    /**
     * 原文链接
     */
    @Size(max = 512, message = "原文链接长度不能超过512个字符")
    @Schema(description = "原文链接", example = "https://juejin.cn/post/123", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String sourceUrl;

    /**
     * 文章内容
     */
    @NotBlank(message = "文章内容不能为空")
    @SecureContent(contentType = ContentSecurityType.MARKDOWN, allowEmpty = false)
    @Schema(description = "文章内容", example = "本文详细介绍了……", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    /**
     * 状态：0-未发布，1-已发布
     */
    @NotNull(message = "文章状态不能为空")
    @Schema(description = "文章状态，0-未发布，1-已发布", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private PublishStatusEnum status;
}