package top.harrylei.community.web.article;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.model.article.dto.ArticleDTO;
import top.harrylei.community.api.model.article.req.*;
import top.harrylei.community.api.model.article.vo.ArticleDetailVO;
import top.harrylei.community.api.model.article.vo.ArticleVO;
import top.harrylei.community.api.model.article.vo.ArticleVersionVO;
import top.harrylei.community.api.model.article.vo.VersionDiffVO;
import top.harrylei.community.api.model.base.Result;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.api.model.statistics.dto.ArticleStatisticsDTO;
import top.harrylei.community.api.model.user.dto.UserInfoDTO;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.security.permission.RequiresLogin;
import top.harrylei.community.service.article.converted.ArticleStructMapper;
import top.harrylei.community.service.article.service.ArticleCommandService;
import top.harrylei.community.service.article.service.ArticleQueryService;
import top.harrylei.community.service.article.service.ArticleVersionService;
import top.harrylei.community.service.statistics.converted.ArticleStatisticsStructMapper;
import top.harrylei.community.service.statistics.service.ArticleStatisticsService;
import top.harrylei.community.service.user.converted.UserStructMapper;
import top.harrylei.community.service.user.service.UserFootService;
import top.harrylei.community.service.user.service.cache.UserCacheService;

import java.util.List;

/**
 * 文章相关模块
 *
 * @author harry
 */
@Tag(name = "文章相关模块", description = "提供文章的基础查询")
@Slf4j
@RestController
@RequestMapping("/v1/article")
@RequiredArgsConstructor
@Validated
public class ArticleController {

    private final ArticleCommandService articleCommandService;
    private final ArticleQueryService articleQueryService;
    private final ArticleStructMapper articleStructMapper;
    private final ArticleVersionService articleVersionService;
    private final ArticleStatisticsService articleStatisticsService;
    private final ArticleStatisticsStructMapper articleStatisticsStructMapper;
    private final UserCacheService userCacheService;
    private final UserStructMapper userStructMapper;
    private final UserFootService userFootService;
    /**
     * 用户新建文章
     *
     * @param articleSaveReq 文章信息请求
     * @return 新建文章ID
     */
    @RequiresLogin
    @Operation(summary = "新建文章", description = "用户保存文章（支持草稿/提交审核）")
    @PostMapping
    public Result<Long> save(@Valid @RequestBody ArticleSaveReq articleSaveReq) {
        ArticleDTO article = articleStructMapper.toDTO(articleSaveReq);
        article.setUserId(ReqInfoContext.getContext().getUserId());
        Long articleId = articleCommandService.saveArticle(article);
        return Result.success(articleId);
    }

    /**
     * 编辑文章
     *
     * @param articleUpdateReq 文章更新请求
     * @return 文章VO
     */
    @RequiresLogin
    @Operation(summary = "编辑文章", description = "用户编辑文章（支持保存草稿和发布）")
    @PutMapping
    public Result<ArticleVO> update(@Valid @RequestBody ArticleUpdateReq articleUpdateReq) {
        ArticleDTO articleDTO = articleStructMapper.toDTO(articleUpdateReq);
        ArticleDTO updatedDTO = articleCommandService.updateArticle(articleDTO);
        ArticleVO article = articleStructMapper.toVO(updatedDTO);
        return Result.success(article);
    }

    /**
     * 删除文章
     *
     * @param articleId 文章ID
     * @return 操作结果
     */
    @RequiresLogin
    @Operation(summary = "删除文章", description = "用户删除文章")
    @DeleteMapping("/{articleId}")
    public Result<Void> delete(@PathVariable Long articleId) {
        articleCommandService.deleteArticle(articleId);
        return Result.success();
    }

    /**
     * 恢复文章
     *
     * @param articleId 文章ID
     * @return 操作结果
     */
    @RequiresLogin
    @Operation(summary = "恢复文章", description = "用户恢复文章")
    @PutMapping("/{articleId}/restore")
    public Result<Void> restore(@PathVariable Long articleId) {
        articleCommandService.restoreArticle(articleId);
        return Result.success();
    }

    /**
     * 文章详细
     *
     * @param articleId 文章ID
     * @return 文章详细
     */
    @Operation(summary = "文章详细", description = "查询文章详细（支持未登录用户访问已发布文章）")
    @GetMapping("/{articleId}")
    public Result<ArticleDetailVO> detail(@PathVariable Long articleId) {
        // 查询文章基础信息
        ArticleDTO articleDTO = articleQueryService.getPublishedArticle(articleId);
        // 查询文章统计信息
        ArticleStatisticsDTO statistics = articleStatisticsService.getArticleStatistics(articleId);
        // 查询作者信息
        UserInfoDTO author = userCacheService.getUserInfo(articleDTO.getUserId());

        // 记录阅读行为
        articleStatisticsService.incrementReadCount(articleId);
        if (ReqInfoContext.getContext().isLoggedIn()) {
            userFootService.recordRead(ReqInfoContext.getContext().getUserId(), articleDTO.getUserId(), articleId);
        }

        // 组装VO
        ArticleDetailVO result = new ArticleDetailVO()
                .setArticle(articleStructMapper.toVO(articleDTO))
                .setAuthor(userStructMapper.toVO(author))
                .setStatistics(articleStatisticsStructMapper.toVO(statistics));

        return Result.success(result);
    }

    /**
     * 文章草稿
     *
     * @param articleId 文章ID
     * @return 文章草稿内容
     */
    @RequiresLogin
    @Operation(summary = "文章草稿", description = "获取文章草稿内容（用于编辑，仅作者可访问）")
    @GetMapping("/{articleId}/draft")
    public Result<ArticleVO> draft(@NotNull(message = "文章ID不能为空") @PathVariable Long articleId) {
        ArticleDTO article = articleQueryService.getLatestArticle(articleId);
        return Result.success(articleStructMapper.toVO(article));
    }

    /**
     * 发布文章
     *
     * @param articleId 文章ID
     * @return 操作结果
     */
    @RequiresLogin
    @Operation(summary = "发布文章", description = "用户发布文章")
    @PostMapping("/{articleId}/publish")
    public Result<Void> publish(@NotNull(message = "文章ID不能为空") @PathVariable Long articleId) {
        articleCommandService.publishArticle(articleId);
        return Result.success();
    }

    /**
     * 撤销发布
     *
     * @param articleId 文章ID
     * @return 操作结果
     */
    @RequiresLogin
    @Operation(summary = "撤销发布", description = "用户撤销文章发布")
    @PostMapping("/{articleId}/unpublish")
    public Result<Void> unpublish(@NotNull(message = "文章ID不能为空") @PathVariable Long articleId) {
        articleCommandService.unpublishArticle(articleId);
        return Result.success();
    }


    /**
     * 文章分页查询
     * <p>
     * 支持多种查询模式：
     * - 公开查询：不传 onlyMine 或 onlyMine=false，查询所有公开文章
     * - 我的文章：onlyMine=true，查询当前用户的文章（需登录）
     * - 指定用户：传 userId，查询指定用户的公开文章
     * - 管理员：可查询所有文章，包括已删除和各种状态
     *
     * @param queryParam 查询参数
     * @return 分页查询结果
     */
    @Operation(summary = "分页查询", description = "智能分页查询，支持公开查询、我的文章、指定用户文章等多种模式")
    @GetMapping("/page")
    public Result<PageVO<ArticleVO>> pageQuery(@Valid ArticleQueryParam queryParam) {
        PageVO<ArticleVO> page = articleQueryService.pageQuery(queryParam);
        return Result.success(page);
    }

    /**
     * 文章操作
     *
     * @param req 文章操作请求
     * @return 操作结果
     */
    @Operation(summary = "文章操作", description = "对文章进行点赞、收藏等操作")
    @RequiresLogin
    @PutMapping("/action")
    public Result<Void> action(@Valid @RequestBody ArticleActionReq req) {
        // 验证操作类型，只允许点赞收藏相关操作
        if (!req.getType().isPraiseOrCollection()) {
            ResultCode.INVALID_PARAMETER.throwException();
        }

        Long userId = ReqInfoContext.getContext().getUserId();
        articleCommandService.actionArticle(userId, req.getArticleId(), req.getType());
        return Result.success();
    }

    /**
     * 获取文章版本历史
     *
     * @param articleId 文章ID
     * @return 版本历史列表
     */
    @Operation(summary = "版本历史", description = "获取文章的版本历史记录")
    @RequiresLogin
    @GetMapping("/{articleId}/versions")
    public Result<List<ArticleVersionVO>> getVersionHistory(@NotNull(message = "文章ID不能为空") @PathVariable Long articleId) {
        List<ArticleVersionVO> versions = articleVersionService.getVersionHistory(articleId);
        return Result.success(versions);
    }

    /**
     * 获取特定版本详情
     *
     * @param articleId 文章ID
     * @param version   版本号
     * @return 版本详情
     */
    @Operation(summary = "版本详情", description = "获取文章指定版本的完整内容")
    @RequiresLogin
    @GetMapping("/{articleId}/versions/{version}")
    public Result<ArticleVO> getVersionDetail(
            @NotNull(message = "文章ID不能为空") @PathVariable Long articleId,
            @NotNull(message = "版本号不能为空") @PathVariable Integer version) {
        // Service返回DTO，转换为VO
        ArticleDTO articleDTO = articleVersionService.getVersionDetail(articleId, version);
        return Result.success(articleStructMapper.toVO(articleDTO));
    }

    /**
     * 对比两个版本
     *
     * @param articleId  文章ID
     * @param compareReq 版本对比请求参数
     * @return 版本对比结果
     */
    @Operation(summary = "版本对比", description = "对比文章的两个版本，显示差异内容")
    @RequiresLogin
    @GetMapping("/{articleId}/versions/compare")
    public Result<VersionDiffVO> compareVersions(
            @NotNull(message = "文章ID不能为空") @PathVariable Long articleId, @Valid VersionCompareReq compareReq) {
        VersionDiffVO diff = articleVersionService.compareVersions(articleId,
                compareReq.getVersion1(),
                compareReq.getVersion2());
        return Result.success(diff);
    }

    /**
     * 回滚到指定版本
     *
     * @param articleId 文章ID
     * @param version   目标版本号
     * @return 回滚后的文章详情
     */
    @Operation(summary = "版本回滚", description = "将文章回滚到指定历史版本作为新的草稿")
    @RequiresLogin
    @PostMapping("/{articleId}/versions/{version}/rollback")
    public Result<ArticleVO> rollbackVersion(
            @NotNull(message = "文章ID不能为空") @PathVariable Long articleId,
            @NotNull(message = "版本号不能为空") @PathVariable Integer version) {
        // Service返回DTO，转换为VO
        ArticleDTO articleDTO = articleCommandService.rollbackToVersion(articleId, version);
        return Result.success(articleStructMapper.toVO(articleDTO));
    }
}
