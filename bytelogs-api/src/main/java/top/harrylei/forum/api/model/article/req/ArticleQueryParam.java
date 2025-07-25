package top.harrylei.forum.api.model.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import top.harrylei.forum.api.model.base.BasePage;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    @Schema(description = "文章状态", example = "1")
    private Integer status;

    /**
     * 删除标识（管理员功能）
     */
    @Schema(description = "删除标识", example = "0")
    private Integer deleted;

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

    /**
     * 字段映射关系
     */
    private static final Map<String, String> FIELD_MAPPING = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("createTime", "create_time"),
            Map.entry("updateTime", "update_time"),
            Map.entry("title", "title"),
            Map.entry("userId", "user_id"),
            Map.entry("userName", "user_name"),
            Map.entry("categoryId", "category_id"),
            Map.entry("status", "status"),
            Map.entry("deleted", "deleted"),
            Map.entry("topping", "topping"),
            Map.entry("cream", "cream")
    );

    /**
     * 获取字段映射关系
     */
    @Override
    public Map<String, String> getFieldMapping() {
        return FIELD_MAPPING;
    }
}