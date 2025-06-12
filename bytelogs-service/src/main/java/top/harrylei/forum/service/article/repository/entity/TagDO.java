package top.harrylei.forum.service.article.repository.entity;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.entity.BaseDO;

/**
 * 标签实体对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tag")
@Accessors(chain = true)
public class TagDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签类型：1-系统标签，2-自定义标签
     */
    private Integer tagType;

    /**
     * 所属类目ID
     */
    private Long categoryId;

    /**
     * 状态：0-未发布，1-已发布
     */
    private Integer status;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer deleted;
}