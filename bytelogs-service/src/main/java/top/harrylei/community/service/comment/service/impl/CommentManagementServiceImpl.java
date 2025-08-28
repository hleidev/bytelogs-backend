package top.harrylei.community.service.comment.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.harrylei.community.api.model.comment.req.CommentManagementQueryParam;
import top.harrylei.community.api.model.comment.vo.CommentManagementVO;
import top.harrylei.community.core.util.PageUtils;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.service.comment.repository.dao.CommentDAO;
import top.harrylei.community.service.comment.service.CommentManagementService;
import top.harrylei.community.service.comment.service.CommentService;

import java.util.List;


/**
 * 评论管理服务实现类
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentManagementServiceImpl implements CommentManagementService {

    private final CommentDAO commentDAO;
    private final CommentService commentService;

    @Override
    public PageVO<CommentManagementVO> pageQuery(CommentManagementQueryParam queryParam) {
        IPage<CommentManagementVO> page = PageUtils.of(queryParam);
        IPage<CommentManagementVO> result = commentDAO.pageQueryForManagement(queryParam, page);

        return PageUtils.from(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComments(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return;
        }

        log.info("管理员批量删除评论，commentIds={}", commentIds);

        for (Long commentId : commentIds) {
            try {
                commentService.deleteComment(commentId);
            } catch (Exception e) {
                log.error("批量删除评论失败，commentId={}", commentId, e);
                throw e;
            }
        }

        log.info("批量删除评论完成，总数={}", commentIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreComments(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return;
        }

        log.info("管理员批量恢复评论，commentIds={}", commentIds);

        for (Long commentId : commentIds) {
            try {
                commentService.restoreComment(commentId);
            } catch (Exception e) {
                log.error("批量恢复评论失败，commentId={}", commentId, e);
                throw e;
            }
        }

        log.info("批量恢复评论完成，总数={}", commentIds.size());
    }
}