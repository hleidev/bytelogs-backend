package top.harrylei.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.api.enums.comment.ContentTypeEnum;
import top.harrylei.forum.service.user.repository.entity.UserFootDO;
import top.harrylei.forum.service.user.repository.mapper.UserFootMapper;

/**
 * 用户足迹访问对象
 *
 * @author harry
 */
@Repository
public class UserFootDAO extends ServiceImpl<UserFootMapper, UserFootDO> {

    public UserFootDO getByContentAndUserId(Long userId, Long contentId, Integer type) {
        return lambdaQuery()
                .eq(UserFootDO::getUserId, userId)
                .eq(UserFootDO::getContentId, contentId)
                .eq(UserFootDO::getContentType, type)
                .one();
    }

    /**
     * 统计文章点赞数量
     *
     * @param articleId 文章ID
     * @return 点赞数量
     */
    public Long countPraiseByArticleId(Long articleId) {
        return lambdaQuery()
                .eq(UserFootDO::getContentId, articleId)
                .eq(UserFootDO::getContentType, ContentTypeEnum.ARTICLE.getCode())
                .eq(UserFootDO::getPraiseState, 1)
                .eq(UserFootDO::getDeleted, 0)
                .count();
    }

    /**
     * 统计文章收藏数量
     *
     * @param articleId 文章ID
     * @return 收藏数量
     */
    public Long countCollectionByArticleId(Long articleId) {
        return lambdaQuery()
                .eq(UserFootDO::getContentId, articleId)
                .eq(UserFootDO::getContentType, ContentTypeEnum.ARTICLE.getCode())
                .eq(UserFootDO::getCollectionState, 1)
                .eq(UserFootDO::getDeleted, 0)
                .count();
    }

}