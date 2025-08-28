package top.harrylei.community.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.harrylei.community.api.model.base.ResVO;
import top.harrylei.community.api.model.article.req.ArticleAuditReq;
import top.harrylei.community.api.model.article.req.ArticleQueryParam;
import top.harrylei.community.api.model.article.req.ArticleStatusUpdateReq;
import top.harrylei.community.api.model.article.vo.ArticleDetailVO;
import top.harrylei.community.api.model.article.vo.ArticleVO;
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
    public ResVO<PageVO<ArticleVO>> pageQuery(@Valid ArticleQueryParam queryParam) {
        PageVO<ArticleVO> page = articleQueryService.pageQuery(queryParam);
        return ResVO.ok(page);
    }

    /**
     * 文章详细
     *
     * @return 文章详细展示对象
     */
    @Operation(summary = "文章详细", description = "管理端文章详细信息")
    @GetMapping("/{articleId}")
    public ResVO<ArticleDetailVO> detail(@NotNull(message = "文章ID不能为空") @PathVariable Long articleId) {
        ArticleDetailVO articleDetail = articleQueryService.getArticleDetail(articleId);
        return ResVO.ok(articleDetail);
    }

    /**
     * 审核文章
     *
     * @param request 审核请求
     * @return 操作结果
     */
    @Operation(summary = "审核文章", description = "管理员审核文章，支持单个和批量操作")
    @PutMapping("/audit")
    public ResVO<Void> auditArticles(@RequestBody @Valid ArticleAuditReq request) {
        articleManagementService.auditArticles(request.getArticleIds(), request.getStatus());
        return ResVO.ok();
    }

    /**
     * 删除文章
     *
     * @param articleIds 文章ID列表
     * @return 操作结果
     */
    @Operation(summary = "删除文章", description = "管理员删除文章，支持单个和批量操作")
    @DeleteMapping
    public ResVO<Void> deleteArticles(@NotNull(message = "文章ID列表不能为空") @RequestBody List<Long> articleIds) {
        articleManagementService.deleteArticles(articleIds);
        return ResVO.ok();
    }

    @Operation(summary = "恢复文章", description = "管理员恢复已删除文章，支持单个和批量操作")
    @PutMapping("/restore")
    public ResVO<Void> restoreArticles(@NotNull(message = "文章ID列表不能为空") @RequestBody List<Long> articleIds) {
        articleManagementService.restoreArticles(articleIds);
        return ResVO.ok();
    }

    /**
     * 更新文章属性标识（置顶/加精/官方）
     *
     * @param request 属性更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新属性", description = "管理员更新文章属性标识（置顶/加精/官方），支持批量操作")
    @PutMapping("/property")
    public ResVO<Void> updateArticleProperty(@RequestBody @Valid ArticleStatusUpdateReq request) {
        articleManagementService.updateArticleProperty(request.getArticleIds(),
                                                       request.getStatusType(),
                                                       request.getStatus());
        return ResVO.ok();
    }
}
