package top.harrylei.community.service.article;

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
import top.harrylei.community.api.enums.article.ArticlePublishStatusEnum;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.enums.user.OperateTypeEnum;
import top.harrylei.community.api.exception.BusinessException;
import top.harrylei.community.api.model.article.dto.ArticleDTO;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.KafkaEventPublisher;
import top.harrylei.community.service.article.converted.ArticleStructMapper;
import top.harrylei.community.service.article.repository.dao.ArticleDAO;
import top.harrylei.community.service.article.repository.dao.ArticleDetailDAO;
import top.harrylei.community.service.article.repository.entity.ArticleDO;
import top.harrylei.community.service.article.repository.entity.ArticleDetailDO;
import top.harrylei.community.service.article.service.ArticleTagService;
import top.harrylei.community.service.article.service.impl.ArticleCommandServiceImpl;
import top.harrylei.community.service.user.service.UserFollowService;
import top.harrylei.community.service.user.service.UserFootService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 文章命令服务测试
 *
 * @author harry
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleCommandServiceImpl 测试")
class ArticleCommandServiceImplTest {

    @Mock
    private ArticleDAO articleDAO;

    @Mock
    private ArticleDetailDAO articleDetailDAO;

    @Mock
    private ArticleStructMapper articleStructMapper;

    @Mock
    private ArticleTagService articleTagService;

    @Mock
    private UserFootService userFootService;

    @Mock
    private UserFollowService userFollowService;

    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    @InjectMocks
    private ArticleCommandServiceImpl articleCommandService;

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
    @DisplayName("saveArticle 方法测试")
    class SaveArticleTest {

        @Test
        @DisplayName("普通用户保存草稿应成功")
        void shouldSaveDraftSuccessfully() {
            // Given
            Long userId = 1L;
            setupNormalUserContext(userId);

            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setTitle("Test Article");
            articleDTO.setContent("Test Content");
            articleDTO.setStatus(ArticlePublishStatusEnum.DRAFT);
            articleDTO.setTagIds(List.of(1L, 2L));

            ArticleDO articleDO = new ArticleDO();
            articleDO.setId(1L);
            articleDO.setUserId(userId);

            when(articleStructMapper.toDO(articleDTO)).thenReturn(articleDO);
            when(articleDAO.insertArticle(any(ArticleDO.class))).thenReturn(1L);
            when(articleStructMapper.toDetailDO(articleDTO)).thenReturn(new ArticleDetailDO());

            // When
            Long articleId = articleCommandService.saveArticle(articleDTO);

            // Then
            assertThat(articleId).isEqualTo(1L);
            verify(articleDAO).insertArticle(any(ArticleDO.class));
            verify(articleDetailDAO).save(any(ArticleDetailDO.class));
            verify(articleTagService).saveBatch(eq(1L), eq(List.of(1L, 2L)));
        }

        @Test
        @DisplayName("普通用户发布文章应变为待审核状态")
        void normalUserPublishShouldBecomeReview() {
            // Given
            Long userId = 1L;
            setupNormalUserContext(userId);

            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setTitle("Test Article");
            articleDTO.setStatus(ArticlePublishStatusEnum.PUBLISHED);

            ArticleDO articleDO = new ArticleDO();
            articleDO.setId(1L);
            articleDO.setUserId(userId);

            ArticleDetailDO detailDO = new ArticleDetailDO();

            when(articleStructMapper.toDO(articleDTO)).thenReturn(articleDO);
            when(articleDAO.insertArticle(any(ArticleDO.class))).thenReturn(1L);
            when(articleStructMapper.toDetailDO(articleDTO)).thenReturn(detailDO);

            // When
            articleCommandService.saveArticle(articleDTO);

            // Then
            verify(articleDetailDAO).save(argThat(detail ->
                    ArticlePublishStatusEnum.REVIEW.equals(detail.getStatus())
            ));
        }

        @Test
        @DisplayName("管理员发布文章应直接发布")
        void adminPublishShouldBePublishedDirectly() {
            // Given
            Long userId = 1L;
            setupAdminContext(userId);

            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setTitle("Admin Article");
            articleDTO.setStatus(ArticlePublishStatusEnum.PUBLISHED);

            ArticleDO articleDO = new ArticleDO();
            articleDO.setId(1L);
            articleDO.setUserId(userId);

            ArticleDetailDO detailDO = new ArticleDetailDO();

            when(articleStructMapper.toDO(articleDTO)).thenReturn(articleDO);
            when(articleDAO.insertArticle(any(ArticleDO.class))).thenReturn(1L);
            when(articleStructMapper.toDetailDO(articleDTO)).thenReturn(detailDO);
            when(userFollowService.listFollowerIds(userId)).thenReturn(Collections.emptyList());

            // When
            articleCommandService.saveArticle(articleDTO);

            // Then
            verify(articleDetailDAO).save(argThat(detail ->
                    ArticlePublishStatusEnum.PUBLISHED.equals(detail.getStatus())
            ));
        }
    }

    @Nested
    @DisplayName("updateArticle 方法测试")
    class UpdateArticleTest {

        @Test
        @DisplayName("文章作者应能更新文章")
        void authorShouldUpdateArticle() {
            // Given
            Long userId = 1L;
            Long articleId = 100L;
            setupNormalUserContext(userId);

            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setId(articleId);
            articleDTO.setTitle("Updated Title");
            articleDTO.setStatus(ArticlePublishStatusEnum.DRAFT);
            articleDTO.setTagIds(List.of(1L));

            ArticleDO articleDO = new ArticleDO();
            articleDO.setId(articleId);
            articleDO.setUserId(userId);
            articleDO.setVersionCount(1);

            ArticleDetailDO newDetailDO = new ArticleDetailDO();

            when(articleDAO.getArticle(articleId, DeleteStatusEnum.NOT_DELETED)).thenReturn(articleDO);
            when(articleStructMapper.toDetailDO(articleDTO)).thenReturn(newDetailDO);
            when(articleStructMapper.buildArticleDTO(any(ArticleDO.class), any(ArticleDetailDO.class)))
                    .thenReturn(articleDTO);

            // When
            ArticleDTO result = articleCommandService.updateArticle(articleDTO);

            // Then
            assertThat(result).isNotNull();
            verify(articleDAO).updateById(any(ArticleDO.class));
            verify(articleDetailDAO).clearLatestFlag(articleId);
            verify(articleDetailDAO).save(any(ArticleDetailDO.class));
        }

        @Test
        @DisplayName("非作者非管理员应抛出权限异常")
        void nonAuthorNonAdminShouldThrowForbidden() {
            // Given
            Long currentUserId = 1L;
            Long authorUserId = 2L;
            Long articleId = 100L;
            setupNormalUserContext(currentUserId);

            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setId(articleId);

            ArticleDO articleDO = new ArticleDO();
            articleDO.setId(articleId);
            articleDO.setUserId(authorUserId); // 不同的作者

            when(articleDAO.getArticle(articleId, DeleteStatusEnum.NOT_DELETED)).thenReturn(articleDO);

            // When & Then
            assertThatThrownBy(() -> articleCommandService.updateArticle(articleDTO))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.FORBIDDEN.getCode());
                    });
        }

        @Test
        @DisplayName("articleId 为 null 应抛出参数异常")
        void nullArticleIdShouldThrowException() {
            // Given
            setupNormalUserContext(1L);

            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setId(null);

            // When & Then
            assertThatThrownBy(() -> articleCommandService.updateArticle(articleDTO))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.INVALID_PARAMETER.getCode());
                    });
        }
    }

    @Nested
    @DisplayName("deleteArticle 方法测试")
    class DeleteArticleTest {

        @Test
        @DisplayName("文章作者应能删除文章")
        void authorShouldDeleteArticle() {
            // Given
            Long userId = 1L;
            Long articleId = 100L;
            setupNormalUserContext(userId);

            ArticleDO articleDO = new ArticleDO();
            articleDO.setId(articleId);
            articleDO.setUserId(userId);

            when(articleDAO.getArticle(articleId, DeleteStatusEnum.NOT_DELETED)).thenReturn(articleDO);

            // When
            articleCommandService.deleteArticle(articleId);

            // Then
            verify(articleDAO).updateDeleted(articleId, DeleteStatusEnum.DELETED);
            verify(articleDetailDAO).updateDeleted(articleId, DeleteStatusEnum.DELETED);
        }

        @Test
        @DisplayName("文章不存在应抛出异常")
        void shouldThrowExceptionWhenArticleNotExists() {
            // Given
            Long articleId = 999L;
            setupNormalUserContext(1L);

            when(articleDAO.getArticle(articleId, DeleteStatusEnum.NOT_DELETED)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> articleCommandService.deleteArticle(articleId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.ARTICLE_NOT_EXISTS.getCode());
                    });
        }

        @Test
        @DisplayName("管理员应能删除任意文章")
        void adminShouldDeleteAnyArticle() {
            // Given
            Long adminId = 1L;
            Long articleId = 100L;
            Long authorId = 2L;
            setupAdminContext(adminId);

            ArticleDO articleDO = new ArticleDO();
            articleDO.setId(articleId);
            articleDO.setUserId(authorId);

            when(articleDAO.getArticle(articleId, DeleteStatusEnum.NOT_DELETED)).thenReturn(articleDO);

            // When
            articleCommandService.deleteArticle(articleId);

            // Then
            verify(articleDAO).updateDeleted(articleId, DeleteStatusEnum.DELETED);
        }
    }

    @Nested
    @DisplayName("restoreArticle 方法测试")
    class RestoreArticleTest {

        @Test
        @DisplayName("应能恢复已删除的文章")
        void shouldRestoreDeletedArticle() {
            // Given
            Long userId = 1L;
            Long articleId = 100L;
            setupNormalUserContext(userId);

            ArticleDO articleDO = new ArticleDO();
            articleDO.setId(articleId);
            articleDO.setUserId(userId);

            when(articleDAO.getArticle(articleId, DeleteStatusEnum.DELETED)).thenReturn(articleDO);

            // When
            articleCommandService.restoreArticle(articleId);

            // Then
            verify(articleDAO).updateDeleted(articleId, DeleteStatusEnum.NOT_DELETED);
            verify(articleDetailDAO).updateDeleted(articleId, DeleteStatusEnum.NOT_DELETED);
        }
    }

    @Nested
    @DisplayName("actionArticle 方法测试")
    class ActionArticleTest {

        @Test
        @DisplayName("点赞操作应成功")
        void praiseShouldSucceed() {
            // Given
            Long userId = 1L;
            Long articleId = 100L;
            Long authorId = 2L;
            setupNormalUserContext(userId);

            ArticleDO articleDO = new ArticleDO();
            articleDO.setId(articleId);
            articleDO.setUserId(authorId);

            when(articleDAO.getArticle(articleId, DeleteStatusEnum.NOT_DELETED)).thenReturn(articleDO);

            // When
            articleCommandService.actionArticle(userId, articleId, OperateTypeEnum.PRAISE);

            // Then
            verify(userFootService).actionArticle(userId, OperateTypeEnum.PRAISE, authorId, articleId);
        }
    }

    @Nested
    @DisplayName("rollbackToVersion 方法测试")
    class RollbackToVersionTest {

        @Test
        @DisplayName("应能回滚到指定版本")
        void shouldRollbackToSpecifiedVersion() {
            // Given
            Long userId = 1L;
            Long articleId = 100L;
            Integer targetVersion = 2;
            setupNormalUserContext(userId);

            ArticleDO articleDO = new ArticleDO();
            articleDO.setId(articleId);
            articleDO.setUserId(userId);
            articleDO.setVersionCount(3);

            ArticleDetailDO targetDetail = new ArticleDetailDO();
            targetDetail.setArticleId(articleId);
            targetDetail.setVersion(targetVersion);
            targetDetail.setContent("Old content");

            ArticleDetailDO newDetail = new ArticleDetailDO();
            newDetail.setArticleId(articleId);
            newDetail.setVersion(4);

            when(articleDAO.getArticle(articleId, DeleteStatusEnum.NOT_DELETED)).thenReturn(articleDO);
            when(articleDetailDAO.getByArticleIdAndVersion(articleId, targetVersion)).thenReturn(targetDetail);
            when(articleStructMapper.copyForNewVersion(targetDetail)).thenReturn(newDetail);
            when(articleStructMapper.buildArticleDTO(any(ArticleDO.class), any(ArticleDetailDO.class)))
                    .thenReturn(new ArticleDTO());
            when(articleTagService.listTagIdsByArticleId(articleId)).thenReturn(List.of(1L));

            // When
            ArticleDTO result = articleCommandService.rollbackToVersion(articleId, targetVersion);

            // Then
            assertThat(result).isNotNull();
            verify(articleDetailDAO).clearLatestFlag(articleId);
            verify(articleDetailDAO).save(any(ArticleDetailDO.class));
            verify(articleDAO).updateById(any(ArticleDO.class));
        }

        @Test
        @DisplayName("目标版本不存在应抛出异常")
        void shouldThrowExceptionWhenVersionNotExists() {
            // Given
            Long userId = 1L;
            Long articleId = 100L;
            Integer targetVersion = 99;
            setupNormalUserContext(userId);

            ArticleDO articleDO = new ArticleDO();
            articleDO.setId(articleId);
            articleDO.setUserId(userId);

            when(articleDAO.getArticle(articleId, DeleteStatusEnum.NOT_DELETED)).thenReturn(articleDO);
            when(articleDetailDAO.getByArticleIdAndVersion(articleId, targetVersion)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> articleCommandService.rollbackToVersion(articleId, targetVersion))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(ResultCode.ARTICLE_NOT_EXISTS.getCode());
                    });
        }
    }
}
