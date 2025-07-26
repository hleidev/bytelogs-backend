package top.harrylei.forum.service.comment.repository.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.comment.req.CommentManagementQueryParam;
import top.harrylei.forum.api.model.comment.vo.CommentManagementVO;
import top.harrylei.forum.service.comment.repository.entity.CommentDO;
import top.harrylei.forum.service.comment.repository.mapper.CommentMapper;

import java.util.List;
import java.util.Set;

/**
 * 评论访问对象
 *
 * @author harry
 */
@Repository
public class CommentDAO extends ServiceImpl<CommentMapper, CommentDO> {


    public IPage<CommentDO> pageQuery(Long articleId, IPage<CommentDO> page) {
        return lambdaQuery()
                .eq(CommentDO::getTopCommentId, 0L)
                .eq(CommentDO::getArticleId, articleId)
                .eq(CommentDO::getDeleted, YesOrNoEnum.NO.getCode())
                .page(page);
    }

    public IPage<CommentDO> pageQueryUserComments(Long userId, IPage<CommentDO> page) {
        return lambdaQuery()
                .eq(CommentDO::getUserId, userId)
                .eq(CommentDO::getDeleted, YesOrNoEnum.NO.getCode())
                .page(page);
    }

    public List<CommentDO> listSubComments(Long articleId, Set<Long> topCommentIds) {
        return lambdaQuery()
                .in(CommentDO::getTopCommentId, topCommentIds)
                .eq(CommentDO::getArticleId, articleId)
                .eq(CommentDO::getDeleted, YesOrNoEnum.NO.getCode())
                .orderByDesc(CommentDO::getCreateTime)
                .list();
    }

    public IPage<CommentManagementVO> pageQueryForManagement(CommentManagementQueryParam queryParam,
                                                             IPage<CommentManagementVO> page) {
        return baseMapper.pageQueryForManagement(page, queryParam);
    }
}