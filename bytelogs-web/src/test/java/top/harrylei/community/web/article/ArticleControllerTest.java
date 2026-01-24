package top.harrylei.community.web.article;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import top.harrylei.community.api.enums.article.ArticlePublishStatusEnum;
import top.harrylei.community.api.enums.article.ArticleSourceEnum;
import top.harrylei.community.api.enums.article.ArticleTypeEnum;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.enums.user.OperateTypeEnum;
import top.harrylei.community.api.exception.BusinessException;
import top.harrylei.community.api.model.article.dto.ArticleDTO;
import top.harrylei.community.api.model.article.req.ArticleActionReq;
import top.harrylei.community.api.model.article.req.ArticleSaveReq;
import top.harrylei.community.api.model.article.req.ArticleUpdateReq;
import top.harrylei.community.api.model.article.vo.ArticleVO;
import top.harrylei.community.api.model.article.vo.ArticleVersionVO;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.api.model.statistics.dto.ArticleStatisticsDTO;
import top.harrylei.community.api.model.user.dto.UserInfoDTO;
import top.harrylei.community.core.util.JwtUtil;
import top.harrylei.community.core.util.KafkaEventPublisher;
import top.harrylei.community.core.util.RedisUtil;
import top.harrylei.community.service.article.converted.ArticleStructMapper;
import top.harrylei.community.service.article.service.ArticleCommandService;
import top.harrylei.community.service.article.service.ArticleQueryService;
import top.harrylei.community.service.article.service.ArticleVersionService;
import top.harrylei.community.service.statistics.converted.ArticleStatisticsStructMapper;
import top.harrylei.community.service.statistics.service.ArticleStatisticsService;
import top.harrylei.community.service.user.converted.UserStructMapper;
import top.harrylei.community.service.user.service.UserFootService;
import top.harrylei.community.service.user.service.cache.UserCacheService;
import top.harrylei.community.web.config.TestSecurityConfig;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 文章控制器测试
 *
 * @author harry
 */
@WebMvcTest(ArticleController.class)
@Import(TestSecurityConfig.class)
@DisplayName("ArticleController 测试")
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Service 依赖
    @MockBean
    private ArticleCommandService articleCommandService;

    @MockBean
    private ArticleQueryService articleQueryService;

    @MockBean
    private ArticleVersionService articleVersionService;

    @MockBean
    private ArticleStatisticsService articleStatisticsService;

    @MockBean
    private UserFootService userFootService;

    // Mapper 依赖
    @MockBean
    private ArticleStructMapper articleStructMapper;

    @MockBean
    private ArticleStatisticsStructMapper articleStatisticsStructMapper;

    @MockBean
    private UserStructMapper userStructMapper;

    // 其他依赖
    @MockBean
    private UserCacheService userCacheService;

    @MockBean
    private KafkaEventPublisher kafkaEventPublisher;

    @MockBean
    private RedisUtil redisUtil;

    @MockBean
    private JwtUtil jwtUtil;

    @Nested
    @DisplayName("POST /v1/article 新建文章测试")
    class SaveArticleTest {

        @Test
        @DisplayName("有效请求应创建文章成功")
        void shouldCreateArticleSuccessfully() throws Exception {
            // Given
            ArticleSaveReq req = new ArticleSaveReq();
            req.setTitle("测试文章标题");
            req.setContent("测试文章内容");
            req.setCategoryId(1L);
            req.setArticleType(ArticleTypeEnum.BLOG);
            req.setSource(ArticleSourceEnum.ORIGINAL);
            req.setStatus(ArticlePublishStatusEnum.DRAFT);

            when(articleStructMapper.toDTO(any(ArticleSaveReq.class))).thenReturn(new ArticleDTO());
            when(articleCommandService.saveArticle(any(ArticleDTO.class))).thenReturn(1L);

            // When & Then
            mockMvc.perform(post("/v1/article")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(1));

            verify(articleCommandService).saveArticle(any(ArticleDTO.class));
        }

        @Test
        @DisplayName("缺少必填参数应返回错误")
        void shouldReturnErrorForMissingRequiredParams() throws Exception {
            // Given - 空请求体
            ArticleSaveReq req = new ArticleSaveReq();

            // When & Then
            mockMvc.perform(post("/v1/article")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.INVALID_PARAMETER.getCode()));
        }
    }

    @Nested
    @DisplayName("PUT /v1/article 更新文章测试")
    class UpdateArticleTest {

        @Test
        @DisplayName("有效请求应更新文章成功")
        void shouldUpdateArticleSuccessfully() throws Exception {
            // Given
            ArticleUpdateReq req = new ArticleUpdateReq();
            req.setId(1L);
            req.setTitle("更新后的标题");
            req.setContent("更新后的内容");
            req.setCategoryId(1L);
            req.setArticleType(ArticleTypeEnum.BLOG);
            req.setSource(ArticleSourceEnum.ORIGINAL);
            req.setStatus(ArticlePublishStatusEnum.PUBLISHED);

            ArticleDTO updatedDTO = new ArticleDTO();
            updatedDTO.setId(1L);

            ArticleVO articleVO = new ArticleVO();
            articleVO.setId(1L);

            when(articleStructMapper.toDTO(any(ArticleUpdateReq.class))).thenReturn(new ArticleDTO());
            when(articleCommandService.updateArticle(any(ArticleDTO.class))).thenReturn(updatedDTO);
            when(articleStructMapper.toVO(any(ArticleDTO.class))).thenReturn(articleVO);

            // When & Then
            mockMvc.perform(put("/v1/article")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(1));

            verify(articleCommandService).updateArticle(any(ArticleDTO.class));
        }

        @Test
        @DisplayName("文章不存在应返回错误")
        void shouldReturnErrorWhenArticleNotExists() throws Exception {
            // Given
            ArticleUpdateReq req = new ArticleUpdateReq();
            req.setId(999L);
            req.setTitle("标题");
            req.setContent("内容");
            req.setCategoryId(1L);
            req.setArticleType(ArticleTypeEnum.BLOG);
            req.setSource(ArticleSourceEnum.ORIGINAL);
            req.setStatus(ArticlePublishStatusEnum.DRAFT);

            when(articleStructMapper.toDTO(any(ArticleUpdateReq.class))).thenReturn(new ArticleDTO());
            when(articleCommandService.updateArticle(any(ArticleDTO.class)))
                    .thenThrow(new BusinessException(ResultCode.ARTICLE_NOT_EXISTS.getCode(), "文章不存在"));

            // When & Then
            mockMvc.perform(put("/v1/article")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.ARTICLE_NOT_EXISTS.getCode()));
        }
    }

    @Nested
    @DisplayName("DELETE /v1/article/{articleId} 删除文章测试")
    class DeleteArticleTest {

        @Test
        @DisplayName("有效请求应删除文章成功")
        void shouldDeleteArticleSuccessfully() throws Exception {
            // Given
            doNothing().when(articleCommandService).deleteArticle(anyLong());

            // When & Then
            mockMvc.perform(delete("/v1/article/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(articleCommandService).deleteArticle(1L);
        }

        @Test
        @DisplayName("文章不存在应返回错误")
        void shouldReturnErrorWhenArticleNotExists() throws Exception {
            // Given
            doThrow(new BusinessException(ResultCode.ARTICLE_NOT_EXISTS.getCode(), "文章不存在"))
                    .when(articleCommandService).deleteArticle(999L);

            // When & Then
            mockMvc.perform(delete("/v1/article/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.ARTICLE_NOT_EXISTS.getCode()));
        }
    }

    @Nested
    @DisplayName("GET /v1/article/{articleId} 文章详情测试")
    class GetArticleDetailTest {

        @Test
        @DisplayName("有效请求应返回文章详情")
        void shouldReturnArticleDetail() throws Exception {
            // Given
            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setId(1L);
            articleDTO.setUserId(1L);
            articleDTO.setTitle("测试文章");

            ArticleStatisticsDTO statistics = new ArticleStatisticsDTO();
            UserInfoDTO author = new UserInfoDTO();
            author.setUserId(1L);

            when(articleQueryService.getPublishedArticle(1L)).thenReturn(articleDTO);
            when(articleStatisticsService.getArticleStatistics(1L)).thenReturn(statistics);
            when(userCacheService.getUserInfo(1L)).thenReturn(author);
            when(articleStructMapper.toVO(any(ArticleDTO.class))).thenReturn(new ArticleVO());

            // When & Then
            mockMvc.perform(get("/v1/article/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(articleQueryService).getPublishedArticle(1L);
        }

        @Test
        @DisplayName("文章不存在应返回错误")
        void shouldReturnErrorWhenArticleNotExists() throws Exception {
            // Given
            when(articleQueryService.getPublishedArticle(999L))
                    .thenThrow(new BusinessException(ResultCode.ARTICLE_NOT_EXISTS.getCode(), "文章不存在"));

            // When & Then
            mockMvc.perform(get("/v1/article/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.ARTICLE_NOT_EXISTS.getCode()));
        }
    }

    @Nested
    @DisplayName("GET /v1/article/page 分页查询测试")
    class PageQueryTest {

        @Test
        @DisplayName("分页查询应返回文章列表")
        void shouldReturnPagedArticles() throws Exception {
            // Given
            PageVO<ArticleVO> pageVO = new PageVO<>();
            pageVO.setPageNum(1);
            pageVO.setPageSize(10);
            pageVO.setTotalElements(0L);
            pageVO.setContent(List.of());

            when(articleQueryService.pageQuery(any())).thenReturn(pageVO);

            // When & Then
            mockMvc.perform(get("/v1/article/page")
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.pageNum").value(1));

            verify(articleQueryService).pageQuery(any());
        }
    }

    @Nested
    @DisplayName("PUT /v1/article/action 文章操作测试")
    class ActionArticleTest {

        @Test
        @DisplayName("点赞操作应成功")
        void shouldPraiseArticleSuccessfully() throws Exception {
            // Given
            ArticleActionReq req = new ArticleActionReq();
            req.setArticleId(1L);
            req.setType(OperateTypeEnum.PRAISE);

            doNothing().when(articleCommandService).actionArticle(any(), anyLong(), any());

            // When & Then
            mockMvc.perform(put("/v1/article/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }

        @Test
        @DisplayName("收藏操作应成功")
        void shouldCollectArticleSuccessfully() throws Exception {
            // Given
            ArticleActionReq req = new ArticleActionReq();
            req.setArticleId(1L);
            req.setType(OperateTypeEnum.COLLECTION);

            doNothing().when(articleCommandService).actionArticle(any(), anyLong(), any());

            // When & Then
            mockMvc.perform(put("/v1/article/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }
    }

    @Nested
    @DisplayName("版本管理接口测试")
    class VersionManagementTest {

        @Test
        @DisplayName("获取版本历史应返回版本列表")
        void shouldReturnVersionHistory() throws Exception {
            // Given
            List<ArticleVersionVO> versions = List.of(new ArticleVersionVO());
            when(articleVersionService.getVersionHistory(1L)).thenReturn(versions);

            // When & Then
            mockMvc.perform(get("/v1/article/1/versions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray());

            verify(articleVersionService).getVersionHistory(1L);
        }

        @Test
        @DisplayName("版本回滚应成功")
        void shouldRollbackVersionSuccessfully() throws Exception {
            // Given
            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setId(1L);

            ArticleVO articleVO = new ArticleVO();
            articleVO.setId(1L);

            when(articleCommandService.rollbackToVersion(1L, 1)).thenReturn(articleDTO);
            when(articleStructMapper.toVO(any(ArticleDTO.class))).thenReturn(articleVO);

            // When & Then
            mockMvc.perform(post("/v1/article/1/versions/1/rollback"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(1));

            verify(articleCommandService).rollbackToVersion(1L, 1);
        }
    }

    @Nested
    @DisplayName("发布/撤销发布测试")
    class PublishTest {

        @Test
        @DisplayName("发布文章应成功")
        void shouldPublishArticleSuccessfully() throws Exception {
            // Given
            doNothing().when(articleCommandService).publishArticle(1L);

            // When & Then
            mockMvc.perform(post("/v1/article/1/publish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(articleCommandService).publishArticle(1L);
        }

        @Test
        @DisplayName("撤销发布应成功")
        void shouldUnpublishArticleSuccessfully() throws Exception {
            // Given
            doNothing().when(articleCommandService).unpublishArticle(1L);

            // When & Then
            mockMvc.perform(post("/v1/article/1/unpublish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(articleCommandService).unpublishArticle(1L);
        }
    }

    @Nested
    @DisplayName("恢复文章测试")
    class RestoreArticleTest {

        @Test
        @DisplayName("恢复已删除文章应成功")
        void shouldRestoreArticleSuccessfully() throws Exception {
            // Given
            doNothing().when(articleCommandService).restoreArticle(1L);

            // When & Then
            mockMvc.perform(put("/v1/article/1/restore"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(articleCommandService).restoreArticle(1L);
        }
    }
}
