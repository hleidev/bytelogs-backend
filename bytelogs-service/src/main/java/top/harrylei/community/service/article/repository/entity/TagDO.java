package top.harrylei.community.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.article.TagTypeEnum;
import top.harrylei.community.api.model.base.BaseDO;

import java.io.Serial;

/**
 * 标签实体对象
 *
 * @author harry
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
     * 标签类型：1-系统标签，2-用户标签
     */
    private TagTypeEnum tagType;

    /**
     * 创建者ID（0-系统标签）
     */
    private Long creatorId;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private DeleteStatusEnum deleted;
}