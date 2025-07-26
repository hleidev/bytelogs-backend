package top.harrylei.forum.api.model.user.dto;

import lombok.Data;

/**
 * 文章足迹统计
 *
 * @author harry
 */
@Data
public class ArticleFootCountDTO {

    /**
     * 文章点赞数
     */
    private Long praiseCount;

    /**
     * 文章被收藏数
     */
    private Long collectionCount;

    public ArticleFootCountDTO() {
        this.praiseCount = 0L;
        this.collectionCount = 0L;
    }
}
