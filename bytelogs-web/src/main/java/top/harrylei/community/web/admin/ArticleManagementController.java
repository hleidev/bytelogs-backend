package top.harrylei.community.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.harrylei.community.api.model.article.req.*;
import top.harrylei.community.api.model.article.vo.ArticleDetailVO;
import top.harrylei.community.api.model.article.vo.ArticleVO;
import top.harrylei.community.api.model.base.Result;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.core.security.permission.RequiresAdmin;
import top.harrylei.community.service.article.service.ArticleManagementService;
import top.harrylei.community.service.article.service.ArticleQueryService;

import java.util.List;

/**
 * 文章管理模块
 *
 * @author harry
 */
@Tag(name = "文章管理模块", description = "提供文章后台管理接口")
@Slf4j
@RestController
@RequestMapping("/v1/admin/article")
@RequiredArgsConstructor
@RequiresAdmin
@Validated
public class ArticleManagementController {

    private final ArticleQueryService articleQueryService;
    private final ArticleManagementService articleManagementService;

    /**
     * 文章分页查询
     *
     * @param queryParam 查询参数
     * @return 分页查询结果
     */
    @Operation(summary = "分页查询", description = "管理端分页查询")
    @GetMapping("/page")
    public Result<PageVO<ArticleVO>> pageQuery(@Valid ArticleQueryParam queryParam) {
        PageVO<ArticleVO> page = articleQueryService.pageQuery(queryParam);
        return Result.success(page);
    }

    /**
     * 文章详细
     *
     * @return 文章详细展示对象
     */
    @Operation(summary = "文章详细", description = "管理端文章详细信息")
    @GetMapping("/{articleId}")
    public Result<ArticleDetailVO> detail(@NotNull(message = "文章ID不能为空") @PathVariable Long articleId) {
        ArticleDetailVO articleDetail = articleQueryService.getArticleDetail(articleId);
        return Result.success(articleDetail);
    }

    /**
     * 审核文章
     *
     * @param request 审核请求
     * @return 操作结果
     */
    @Operation(summary = "审核文章", description = "管理员审核文章，支持单个和批量操作")
    @PutMapping("/audit")
    public Result<Void> auditArticles(@RequestBody @Valid ArticleAuditReq request) {
        articleManagementService.auditArticles(request.getArticleIds(), request.getStatus());
        return Result.success();
    }

    /**
     * 删除文章
     *
     * @param articleIds 文章ID列表
     * @return 操作结果
     */
    @Operation(summary = "删除文章", description = "管理员删除文章，支持单个和批量操作")
    @DeleteMapping
    public Result<Void> deleteArticles(@NotNull(message = "文章ID列表不能为空") @RequestBody List<Long> articleIds) {
        articleManagementService.deleteArticles(articleIds);
        return Result.success();
    }

    @Operation(summary = "恢复文章", description = "管理员恢复已删除文章，支持单个和批量操作")
    @PutMapping("/restore")
    public Result<Void> restoreArticles(@NotNull(message = "文章ID列表不能为空") @RequestBody List<Long> articleIds) {
        articleManagementService.restoreArticles(articleIds);
        return Result.success();
    }

    /**
     * 更新置顶标识
     *
     * @param request 属性更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新置顶", description = "管理员更新文章属性标识置顶，支持批量操作")
    @PutMapping("/topping")
    public Result<Void> updateArticleTopping(@RequestBody @Valid ArticleToppingUpdateReq request) {
        articleManagementService.updateArticleTopping(request.getArticleIds(), request.getToppingStat());
        return Result.success();
    }

    /**
     * 更新加精标识
     *
     * @param request 属性更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新加精", description = "管理员更新文章属性标识加精，支持批量操作")
    @PutMapping("/cream")
    public Result<Void> updateArticleCream(@RequestBody @Valid ArticleCreamUpdateReq request) {
        articleManagementService.updateArticleCream(request.getArticleIds(), request.getCreamStat());
        return Result.success();
    }

    /**
     * 更新官方标识
     *
     * @param request 属性更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新官方", description = "管理员更新文章属性标识官方，支持批量操作")
    @PutMapping("/official")
    public Result<Void> updateArticleOfficial(@RequestBody @Valid ArticleOfficialUpdateReq request) {
        articleManagementService.updateArticleOfficial(request.getArticleIds(), request.getOfficialStat());
        return Result.success();
    }
}
