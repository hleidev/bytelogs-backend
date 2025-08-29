package top.harrylei.community.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.article.ArticleTypeEnum;
import top.harrylei.community.api.enums.article.CreamStatusEnum;
import top.harrylei.community.api.enums.article.OfficialStatusEnum;
import top.harrylei.community.api.enums.article.ToppingStatusEnum;
import top.harrylei.community.api.model.base.BaseDO;

import java.io.Serial;

/**
 * 文章实体对象
 *
 * @author harry
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
    private ArticleTypeEnum articleType;

    /**
     * 官方状态：0-非官方，1-官方
     */
    private OfficialStatusEnum official;

    /**
     * 是否置顶：0-不置顶，1-置顶
     */
    private ToppingStatusEnum topping;

    /**
     * 是否加精：0-不加精，1-加精
     */
    private CreamStatusEnum cream;

    /**
     * 版本总数
     */
    private Integer versionCount;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private DeleteStatusEnum deleted;
}