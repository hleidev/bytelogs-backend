package top.harrylei.forum.api.model.vo.comment.req;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import top.harrylei.forum.api.model.entity.BasePage;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.enums.comment.ToppingStatEnum;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 评论管理端分页查询参数
 *
 * @author harry
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "评论管理端分页查询参数")
public class CommentManagementQueryParam extends BasePage {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID，查询指定用户的评论", example = "1")
    private Long userId;

    /**
     * 搜索关键词（全局模糊搜索）
     */
    @Schema(description = "搜索关键词，支持评论内容、用户名、文章标题的模糊搜索", example = "Spring Boot")
    private String keyword;

    /**
     * 删除状态筛选
     */
    @Schema(description = "删除状态筛选：0-未删除，1-已删除，不传查询所有", example = "0")
    private YesOrNoEnum deleted;

    /**
     * 是否为顶级评论筛选
     */
    @Schema(description = "是否为顶级评论：1-顶级评论（TOPPING），0-回复评论（NOT_TOPPING），不传查询所有", example = "1")
    private ToppingStatEnum topComment;

    /**
     * 创建时间范围开始
     */
    @Schema(description = "创建时间开始，格式：yyyy-MM-dd HH:mm:ss", example = "2025-01-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeStart;

    /**
     * 创建时间范围结束
     */
    @Schema(description = "创建时间结束，格式：yyyy-MM-dd HH:mm:ss", example = "2025-12-31 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeEnd;

    /**
     * 获取字段映射关系（前端字段 -> 数据库字段）
     */
    public Map<String, String> getFieldMapping() {
        Map<String, String> mapping = new HashMap<>();
        // 基础字段
        mapping.put("id", "id");
        mapping.put("createTime", "create_time");
        mapping.put("updateTime", "update_time");
        // 评论相关字段
        mapping.put("content", "content");
        mapping.put("userId", "user_id");
        mapping.put("userName", "user_name");
        mapping.put("articleId", "article_id");
        mapping.put("articleTitle", "article_title");
        mapping.put("topCommentId", "top_comment_id");
        mapping.put("parentCommentId", "parent_comment_id");
        mapping.put("deleted", "deleted");
        return mapping;
    }

    /**
     * 创建带排序的分页对象
     */
    public <T> IPage<T> toPage() {
        return super.toPage(getFieldMapping());
    }
}