package top.harrylei.community.service.user.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.article.CollectionStatusEnum;
import top.harrylei.community.api.enums.article.ContentTypeEnum;
import top.harrylei.community.api.enums.user.PraiseStatusEnum;
import top.harrylei.community.service.user.repository.entity.UserFootDO;
import top.harrylei.community.service.user.repository.mapper.UserFootMapper;

/**
 * 用户足迹访问对象
 *
 * @author harry
 */
@Repository
public class UserFootDAO extends ServiceImpl<UserFootMapper, UserFootDO> {

    public UserFootDO getByContentAndUserId(Long userId, Long contentId, ContentTypeEnum type) {
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
                .eq(UserFootDO::getContentType, ContentTypeEnum.ARTICLE)
                .eq(UserFootDO::getPraiseState, PraiseStatusEnum.PRAISE)
                .eq(UserFootDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
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
                .eq(UserFootDO::getContentType, ContentTypeEnum.ARTICLE)
                .eq(UserFootDO::getCollectionState, CollectionStatusEnum.COLLECTION)
                .eq(UserFootDO::getDeleted, DeleteStatusEnum.NOT_DELETED)
                .count();
    }

}