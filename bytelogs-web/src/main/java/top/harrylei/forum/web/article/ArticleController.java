package top.harrylei.forum.web.article;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.harrylei.forum.api.enums.ErrorCodeEnum;
import top.harrylei.forum.api.model.article.dto.ArticleDTO;
import top.harrylei.forum.api.model.article.req.*;
import top.harrylei.forum.api.model.article.vo.ArticleDetailVO;
import top.harrylei.forum.api.model.article.vo.ArticleVO;
import top.harrylei.forum.api.model.article.vo.ArticleVersionVO;
import top.harrylei.forum.api.model.article.vo.VersionDiffVO;
import top.harrylei.forum.api.model.base.ResVO;
import top.harrylei.forum.api.model.page.PageVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.core.security.permission.RequiresLogin;
import top.harrylei.forum.service.article.converted.ArticleStructMapper;
import top.harrylei.forum.service.article.service.ArticleCommandService;
import top.harrylei.forum.service.article.service.ArticleQueryService;
import top.harrylei.forum.service.article.service.ArticleVersionService;

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

    /**
     * 用户新建文章
     *
     * @param articleSaveReq 文章信息请求
     * @return 新建文章ID
     */
    @RequiresLogin
    @Operation(summary = "新建文章", description = "用户保存文章（支持草稿/提交审核）")
    @PostMapping
    public ResVO<Long> save(@Valid @RequestBody ArticleSaveReq articleSaveReq) {
        ArticleDTO articleDTO = articleStructMapper.toDTO(articleSaveReq);
        articleDTO.setUserId(ReqInfoContext.getContext().getUserId());
        Long articleId = articleCommandService.saveArticle(articleDTO);
        return ResVO.ok(articleId);
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
    public ResVO<ArticleVO> update(@Valid @RequestBody ArticleUpdateReq articleUpdateReq) {
        ArticleDTO articleDTO = articleStructMapper.toDTO(articleUpdateReq);
        ArticleVO article = articleCommandService.updateArticle(articleDTO);
        return ResVO.ok(article);
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
    public ResVO<Void> delete(@PathVariable Long articleId) {
        articleCommandService.deleteArticle(articleId);
        return ResVO.ok();
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
    public ResVO<Void> restore(@PathVariable Long articleId) {
        articleCommandService.restoreArticle(articleId);
        return ResVO.ok();
    }

    /**
     * 文章详细
     *
     * @param articleId 文章ID
     * @return 文章详细
     */
    @Operation(summary = "文章详细", description = "查询文章详细（支持未登录用户访问已发布文章）")
    @GetMapping("/{articleId}")
    public ResVO<ArticleDetailVO> detail(@PathVariable Long articleId) {
        ArticleDetailVO vo = articleQueryService.getArticleDetail(articleId);
        return ResVO.ok(vo);
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
    public ResVO<ArticleVO> draft(@NotNull(message = "文章ID不能为空") @PathVariable Long articleId) {
        ArticleVO vo = articleCommandService.getArticleDraft(articleId);
        return ResVO.ok(vo);
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
    public ResVO<Void> publish(@NotNull(message = "文章ID不能为空") @PathVariable Long articleId) {
        articleCommandService.publishArticle(articleId);
        return ResVO.ok();
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
    public ResVO<Void> unpublish(@NotNull(message = "文章ID不能为空") @PathVariable Long articleId) {
        articleCommandService.unpublishArticle(articleId);
        return ResVO.ok();
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
    public ResVO<PageVO<ArticleVO>> pageQuery(@Valid ArticleQueryParam queryParam) {
        PageVO<ArticleVO> page = articleQueryService.pageQuery(queryParam);
        return ResVO.ok(page);
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
    public ResVO<Void> action(@Valid @RequestBody ArticleActionReq req) {
        // 验证操作类型，只允许点赞收藏相关操作
        if (!req.getType().isPraiseOrCollection()) {
            ExceptionUtil.error(ErrorCodeEnum.PARAM_VALIDATE_FAILED, "不支持的操作类型");
        }

        Long userId = ReqInfoContext.getContext().getUserId();
        articleCommandService.actionArticle(userId, req.getArticleId(), req.getType());
        return ResVO.ok();
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
    public ResVO<List<ArticleVersionVO>> getVersionHistory(@NotNull(message = "文章ID不能为空") @PathVariable Long articleId) {
        List<ArticleVersionVO> versions = articleVersionService.getVersionHistory(articleId);
        return ResVO.ok(versions);
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
    public ResVO<ArticleVO> getVersionDetail(
            @NotNull(message = "文章ID不能为空") @PathVariable Long articleId,
            @NotNull(message = "版本号不能为空") @PathVariable Integer version) {
        ArticleVO detail = articleVersionService.getVersionDetail(articleId, version);
        return ResVO.ok(detail);
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
    public ResVO<VersionDiffVO> compareVersions(
            @NotNull(message = "文章ID不能为空") @PathVariable Long articleId, @Valid VersionCompareReq compareReq) {
        VersionDiffVO diff = articleVersionService.compareVersions(articleId,
                                                                   compareReq.getVersion1(),
                                                                   compareReq.getVersion2());
        return ResVO.ok(diff);
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
    public ResVO<ArticleVO> rollbackVersion(
            @NotNull(message = "文章ID不能为空") @PathVariable Long articleId,
            @NotNull(message = "版本号不能为空") @PathVariable Integer version) {
        ArticleVO result = articleCommandService.rollbackToVersion(articleId, version);
        return ResVO.ok(result);
    }
}
