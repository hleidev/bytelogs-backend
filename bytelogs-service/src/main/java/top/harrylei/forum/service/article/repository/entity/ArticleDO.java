package top.harrylei.forum.service.article.repository.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.entity.BaseDO;

/**
 * 文章实体对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article")
@Accessors(chain = true)
public class ArticleDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文章类型：1-博文，2-问答
     */
    private Integer articleType;

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
     * 类目ID
     */
    private Long categoryId;

    /**
     * 来源：1-转载，2-原创，3-翻译
     */
    private Integer source;

    /**
     * 原文链接
     */
    private String sourceUrl;

    /**
     * 官方状态：0-非官方，1-官方
     */
    private Integer official;

    /**
     * 是否置顶：0-不置顶，1-置顶
     */
    private Integer topping;

    /**
     * 是否加精：0-不加精，1-加精
     */
    private Integer cream;

    /**
     * 状态：0-未发布，1-已发布，2-待审核
     */
    private Integer status;

    /**
     * 当前最大版本号（用于生成新版本）
     */
    private Integer currentVersion;

    /**
     * 已发布版本号（0表示未发布）
     */
    private Integer publishedVersion;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer deleted;
}