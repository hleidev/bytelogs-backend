package top.harrylei.community.service.comment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.enums.user.OperateTypeEnum;
import top.harrylei.community.api.exception.BusinessException;
import top.harrylei.community.api.model.article.dto.ArticleDTO;
import top.harrylei.community.api.model.comment.dto.CommentDTO;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.KafkaEventPublisher;
import top.harrylei.community.service.article.repository.dao.ArticleDetailDAO;
import top.harrylei.community.service.article.service.ArticleQueryService;
import top.harrylei.community.service.comment.converted.CommentStructMapper;
import top.harrylei.community.service.comment.repository.dao.CommentDAO;
import top.harrylei.community.service.comment.repository.entity.CommentDO;
import top.harrylei.community.service.comment.service.impl.CommentServiceImpl;
import top.harrylei.community.service.user.service.UserFootService;
import top.harrylei.community.service.user.service.cache.UserCacheService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 评论服务测试
 *
 * @author harry
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl 测试")
class CommentServiceImplTest {

    @Mock
    private CommentDAO commentDAO;

    @Mock
    private CommentStructMapper commentStructMapper;

    @Mock
    private ArticleQueryService articleQueryService;

    @Mock
    private ArticleDetailDAO articleDetailDAO;

    @Mock
    private UserFootService userFootService;

    @Mock
    private UserCacheService userCacheService;

    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    @InjectMocks
    private CommentServiceImpl commentService;

    private MockedStatic<ReqInfoContext> contextMock;

    @BeforeEach
    void setUp() {
        contextMock = mockStatic(ReqInfoContext.class);
    }

    @AfterEach
    void tearDown() {
        if (contextMock != null) {
            contextMock.close();
        }
    }

    private void setupNormalUserContext(Long userId) {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(userId);
        reqInfo.setAuthorities(Collections.emptyList());
        contextMock.when(ReqInfoContext::getContext).thenReturn(reqInfo);
    }

    private void setupAdminContext(Long userId) {
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        reqInfo.setUserId(userId);
        reqInfo.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        contextMock.when(ReqInfoContext::getContext).thenReturn(reqInfo);
    }

    @Nested
    @DisplayName("saveComment 方法测试")
    class SaveCommentTest {

        @Test
        @DisplayName("顶级评论应保存成功")
        void shouldSaveTopLevelComment() {
            // Given
            Long userId = 1L;
            Long articleId = 100L;
            Long authorId = 2L;
            setupNormalUserContext(userId);

            CommentDTO dto = new CommentDTO();
            dto.setArticleId(articleId);
            dto.setContent("Test comment");
            dto.setUserId(userId);
            dto.setParentCommentId(0L);

            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setId(articleId);
            articleDTO.setUserId(authorId);

            CommentDO commentDO = new CommentDO();
            commentDO.setId(1L);
            commentDO.setUserId(userId);
            commentDO.setArticleId(articleId);
            commentDO.setTopCommentId(0L);

            when(articleQueryService.getPublishedArticle(articleId)).thenReturn(articleDTO);
            when(commentStructMapper.toDO(dto)).thenReturn(commentDO);

            // When
            Long commentId = commentService.saveComment(dto);

            // Then
            assertThat(commentId).isEqualTo(1L);
            verify(commentDAO).save(any(CommentDO.class));
            verify(userFootService).saveCommentFoot(any(CommentDO.class), eq(authorId), isNull());
        }

        @Test
        @DisplayName("回复评论应正确设置顶级评论 ID")
        void shouldSetTopCommentIdForReply() {
            // Given
            Long userId = 1L;
            Long articleId = 100L;
            Long parentCommentId = 10L;
            setupNormalUserContext(userId);

            CommentDTO dto = new CommentDTO();
            dto.setArticleId(articleId);
            dto.setContent("Reply comment");
            dto.setUserId(userId);
            dto.setParentCommentId(parentCommentId);

            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setId(articleId);
            articleDTO.setUserId(2L);

            CommentDO parentComment = new CommentDO();
            parentComment.setId(parentCommentId);
            parentComment.setUserId(3L);
            parentComment.setTopCommentId(0L); // 父评论是顶级评论

            CommentDO newComment = new CommentDO();
            newComment.setId(1L);
            newComment.setUserId(userId);
            newComment.setArticleId(articleId);

            when(articleQueryService.getPublishedArticle(articleId)).thenReturn(articleDTO);
            when(commentDAO.getById(parentCommentId)).thenReturn(parentComment);
            when(commentStructMapper.toDO(dto)).thenReturn(newComment);

            // When
            commentService.saveComment(dto);

            // Then
            verify(commentDAO).save(argThat(comment ->
                parentCommentId.equals(comment.getTopCommentId())
            ));
        }

        @Test
        @DisplayName("文章不存在应抛出异常")
        void shouldThrowExceptionWhenArticleNotExists() {
            // Given
            Long articleId = 999L;
            setupNormalUserContext(1L);

            CommentDTO dto = new CommentDTO();
            dto.setArticleId(articleId);

            when(articleQueryService.getPublishedArticle(articleId)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> commentService.saveComment(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.ARTICLE_NOT_EXISTS.getCode());
                    });
        }

        @Test
        @DisplayName("父评论不存在应抛出异常")
        void shouldThrowExceptionWhenParentCommentNotExists() {
            // Given
            Long articleId = 100L;
            Long parentCommentId = 999L;
            setupNormalUserContext(1L);

            CommentDTO dto = new CommentDTO();
            dto.setArticleId(articleId);
            dto.setParentCommentId(parentCommentId);

            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setId(articleId);

            when(articleQueryService.getPublishedArticle(articleId)).thenReturn(articleDTO);
            when(commentDAO.getById(parentCommentId)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> commentService.saveComment(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.COMMENT_NOT_EXISTS.getCode());
                    });
        }
    }

    @Nested
    @DisplayName("updateComment 方法测试")
    class UpdateCommentTest {

        @Test
        @DisplayName("24小时内应能编辑评论")
        void shouldUpdateCommentWithin24Hours() {
            // Given
            Long userId = 1L;
            Long commentId = 100L;
            setupNormalUserContext(userId);

            CommentDTO dto = new CommentDTO();
            dto.setId(commentId);
            dto.setContent("Updated content");

            CommentDO commentDO = new CommentDO();
            commentDO.setId(commentId);
            commentDO.setUserId(userId);
            commentDO.setDeleted(DeleteStatusEnum.NOT_DELETED);
            commentDO.setCreateTime(LocalDateTime.now().minusHours(12)); // 12小时前创建

            when(commentDAO.getById(commentId)).thenReturn(commentDO);

            // When
            commentService.updateComment(dto);

            // Then
            verify(commentDAO).updateById(argThat(comment ->
                "Updated content".equals(comment.getContent())
            ));
        }

        @Test
        @DisplayName("超过24小时应抛出操作不允许异常")
        void shouldThrowExceptionAfter24Hours() {
            // Given
            Long userId = 1L;
            Long commentId = 100L;
            setupNormalUserContext(userId);

            CommentDTO dto = new CommentDTO();
            dto.setId(commentId);
            dto.setContent("Updated content");

            CommentDO commentDO = new CommentDO();
            commentDO.setId(commentId);
            commentDO.setUserId(userId);
            commentDO.setDeleted(DeleteStatusEnum.NOT_DELETED);
            commentDO.setCreateTime(LocalDateTime.now().minusHours(25)); // 25小时前创建

            when(commentDAO.getById(commentId)).thenReturn(commentDO);

            // When & Then
            assertThatThrownBy(() -> commentService.updateComment(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.OPERATION_NOT_ALLOWED.getCode());
                    });
        }

        @Test
        @DisplayName("非作者应抛出权限异常")
        void nonAuthorShouldThrowForbidden() {
            // Given
            Long currentUserId = 1L;
            Long authorUserId = 2L;
            Long commentId = 100L;
            setupNormalUserContext(currentUserId);

            CommentDTO dto = new CommentDTO();
            dto.setId(commentId);

            CommentDO commentDO = new CommentDO();
            commentDO.setId(commentId);
            commentDO.setUserId(authorUserId);
            commentDO.setDeleted(DeleteStatusEnum.NOT_DELETED);

            when(commentDAO.getById(commentId)).thenReturn(commentDO);

            // When & Then
            assertThatThrownBy(() -> commentService.updateComment(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.FORBIDDEN.getCode());
                    });
        }

        @Test
        @DisplayName("已删除评论对普通用户应不可见")
        void deletedCommentShouldBeInvisibleToNormalUser() {
            // Given
            Long userId = 1L;
            Long commentId = 100L;
            setupNormalUserContext(userId);

            CommentDTO dto = new CommentDTO();
            dto.setId(commentId);

            CommentDO commentDO = new CommentDO();
            commentDO.setId(commentId);
            commentDO.setUserId(userId);
            commentDO.setDeleted(DeleteStatusEnum.DELETED);

            when(commentDAO.getById(commentId)).thenReturn(commentDO);

            // When & Then
            assertThatThrownBy(() -> commentService.updateComment(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.COMMENT_NOT_EXISTS.getCode());
                    });
        }
    }

    @Nested
    @DisplayName("deleteComment 方法测试")
    class DeleteCommentTest {

        @Test
        @DisplayName("评论作者应能删除评论")
        void authorShouldDeleteComment() {
            // Given
            Long userId = 1L;
            Long commentId = 100L;
            Long articleId = 200L;
            setupNormalUserContext(userId);

            CommentDO commentDO = new CommentDO();
            commentDO.setId(commentId);
            commentDO.setUserId(userId);
            commentDO.setArticleId(articleId);
            commentDO.setDeleted(DeleteStatusEnum.NOT_DELETED);
            commentDO.setParentCommentId(0L);

            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setUserId(2L);

            when(commentDAO.getById(commentId)).thenReturn(commentDO);
            when(articleQueryService.getPublishedArticle(articleId)).thenReturn(articleDTO);

            // When
            commentService.deleteComment(commentId);

            // Then
            verify(commentDAO).updateById(argThat(comment ->
                DeleteStatusEnum.DELETED.equals(comment.getDeleted())
            ));
        }

        @Test
        @DisplayName("评论不存在应抛出异常")
        void shouldThrowExceptionWhenCommentNotExists() {
            // Given
            Long commentId = 999L;
            setupNormalUserContext(1L);

            when(commentDAO.getById(commentId)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> commentService.deleteComment(commentId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.COMMENT_NOT_EXISTS.getCode());
                    });
        }
    }

    @Nested
    @DisplayName("restoreComment 方法测试")
    class RestoreCommentTest {

        @Test
        @DisplayName("应能恢复已删除的评论")
        void shouldRestoreDeletedComment() {
            // Given
            Long userId = 1L;
            Long commentId = 100L;
            Long articleId = 200L;
            setupNormalUserContext(userId);

            CommentDO commentDO = new CommentDO();
            commentDO.setId(commentId);
            commentDO.setUserId(userId);
            commentDO.setArticleId(articleId);
            commentDO.setDeleted(DeleteStatusEnum.DELETED);
            commentDO.setParentCommentId(0L);

            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setUserId(2L);

            when(commentDAO.getById(commentId)).thenReturn(commentDO);
            when(articleQueryService.getPublishedArticle(articleId)).thenReturn(articleDTO);

            // When
            commentService.restoreComment(commentId);

            // Then
            verify(commentDAO).updateById(argThat(comment ->
                DeleteStatusEnum.NOT_DELETED.equals(comment.getDeleted())
            ));
        }
    }

    @Nested
    @DisplayName("actionComment 方法测试")
    class ActionCommentTest {

        @Test
        @DisplayName("点赞评论应成功")
        void praiseShouldSucceed() {
            // Given
            Long userId = 1L;
            Long commentId = 100L;
            Long authorId = 2L;
            setupNormalUserContext(userId);

            CommentDO commentDO = new CommentDO();
            commentDO.setId(commentId);
            commentDO.setUserId(authorId);

            when(commentDAO.getById(commentId)).thenReturn(commentDO);
            when(userFootService.actionComment(userId, OperateTypeEnum.PRAISE, authorId, commentId))
                    .thenReturn(true);

            // When
            commentService.actionComment(commentId, OperateTypeEnum.PRAISE);

            // Then
            verify(userFootService).actionComment(userId, OperateTypeEnum.PRAISE, authorId, commentId);
        }

        @Test
        @DisplayName("评论不存在应抛出异常")
        void shouldThrowExceptionWhenCommentNotExists() {
            // Given
            Long commentId = 999L;
            setupNormalUserContext(1L);

            when(commentDAO.getById(commentId)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> commentService.actionComment(commentId, OperateTypeEnum.PRAISE))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.COMMENT_NOT_EXISTS.getCode());
                    });
        }

        @Test
        @DisplayName("操作失败应抛出内部错误异常")
        void shouldThrowInternalErrorWhenActionFails() {
            // Given
            Long userId = 1L;
            Long commentId = 100L;
            setupNormalUserContext(userId);

            CommentDO commentDO = new CommentDO();
            commentDO.setId(commentId);
            commentDO.setUserId(2L);

            when(commentDAO.getById(commentId)).thenReturn(commentDO);
            when(userFootService.actionComment(anyLong(), any(), anyLong(), anyLong()))
                    .thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> commentService.actionComment(commentId, OperateTypeEnum.PRAISE))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.INTERNAL_ERROR.getCode());
                    });
        }
    }
}
