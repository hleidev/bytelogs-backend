package top.harrylei.forum.api.model.vo.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import top.harrylei.forum.api.model.vo.user.vo.UserInfoVO;

/**
 * 文章详情对象
 *
 * @author harry
 */
@Data
@Schema(description = "文章详情对象")
@Accessors(chain = true)
public class ArticleDetailVO {

    /**
     * 文章信息
     */
    private ArticleVO article;

    /**
     * 作者信息
     */
    private UserInfoVO author;
}