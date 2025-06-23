package top.harrylei.forum.api.model.vo.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import top.harrylei.forum.api.model.entity.BasePage;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 文章分页查询参数
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "文章分页查询参数")
public class ArticleQueryParam extends BasePage {

    /**
     * 标题关键词（模糊搜索）
     */
    @Schema(description = "标题关键词", example = "Spring Boot")
    private String title;

    /**
     * 作者ID
     */
    @Schema(description = "作者ID", example = "1")
    private Long userId;

    /**
     * 作者昵称（模糊搜索）
     */
    @Schema(description = "作者昵称", example = "张三")
    private String userName;

    /**
     * 分类ID
     */
    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    /**
     * 标签ID列表
     */
    @Schema(description = "标签ID列表", example = "1,2,3")
    private String tagIds;

    /**
     * 文章状态
     */
    @Schema(description = "文章状态")
    private PublishStatusEnum status;

    /**
     * 删除标识（管理员功能）
     */
    @Schema(description = "删除标识", example = "0")
    private YesOrNoEnum deleted;

    /**
     * 是否只查询我的文章
     */
    @Schema(description = "是否只查询我的文章", example = "false")
    private Boolean onlyMine;

    /**
     * 创建时间开始
     */
    @Schema(description = "创建时间开始", example = "2025-01-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeStart;

    /**
     * 创建时间结束
     */
    @Schema(description = "创建时间结束", example = "2025-12-31 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeEnd;

    /**
     * 标签ID列表（用于MyBatis XML访问）
     */
    @Schema(hidden = true)
    private transient List<Long> tagIdList;

    /**
     * 获取标签ID列表
     */
    public List<Long> getTagIdList() {
        if (tagIdList != null) {
            return tagIdList;
        }

        if (StringUtils.hasText(tagIds)) {
            tagIdList = Arrays.stream(tagIds.split(","))
                    .map(String::trim)
                    .map(Long::valueOf)
                    .toList();
            return tagIdList;
        }
        return Collections.emptyList();
    }

    // TODO 排序相关逻辑
}