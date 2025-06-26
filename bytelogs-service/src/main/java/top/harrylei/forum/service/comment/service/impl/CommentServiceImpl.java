package top.harrylei.forum.service.comment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.forum.api.model.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.vo.comment.dto.CommentDTO;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.core.util.NumUtil;
import top.harrylei.forum.service.article.repository.entity.ArticleDO;
import top.harrylei.forum.service.article.service.ArticleService;
import top.harrylei.forum.service.comment.converted.CommentStructMapper;
import top.harrylei.forum.service.comment.repository.dao.CommentDAO;
import top.harrylei.forum.service.comment.repository.entity.CommentDO;
import top.harrylei.forum.service.comment.service.CommentService;
import top.harrylei.forum.service.user.service.UserFootService;

/**
 * 评论服务实现类
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentDAO commentDAO;
    private final CommentStructMapper commentStructMapper;
    private final ArticleService articleService;
    private final UserFootService userFootService;

    /**
     * 保存评论
     *
     * @param dto 评论DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveComment(CommentDTO dto) {
        CommentDO comment = insertComment(dto);
        return comment.getId();
    }

    /**
     * 插入评论
     */
    private CommentDO insertComment(CommentDTO dto) {
        // 验证文章是否存在
        ArticleDO article = articleService.getArticleById(dto.getArticleId());
        ExceptionUtil.requireValid(article, ErrorCodeEnum.ARTICLE_NOT_EXISTS, "articleId=" + dto.getArticleId());

        // 验证父评论是否存在
        CommentDO parent = getParentComment(dto.getParentCommentId());

        // 保存评论
        CommentDO comment = commentStructMapper.toDO(dto);
        commentDAO.save(comment);

        // 保存用户足迹
        Long parentUserId = parent != null ? parent.getUserId() : null;
        userFootService.saveCommentFoot(comment, article.getUserId(), parentUserId);

        return comment;
    }

    private CommentDO getParentComment(Long parentCommentId) {
        if (NumUtil.nullOrZero(parentCommentId)) {
            return null;
        }

        CommentDO parent = commentDAO.getById(parentCommentId);

        ExceptionUtil.requireValid(parent, ErrorCodeEnum.COMMENT_NOT_EXISTS, "parentCommentI=" + parentCommentId);
        return parent;
    }
}