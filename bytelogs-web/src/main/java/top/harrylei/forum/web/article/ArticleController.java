package top.harrylei.forum.web.article;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.article.dto.ArticleDTO;
import top.harrylei.forum.api.model.vo.article.req.ArticlePostReq;
import top.harrylei.forum.api.model.vo.article.req.ArticleQueryParam;
import top.harrylei.forum.api.model.vo.article.req.ArticleUpdateReq;
import top.harrylei.forum.api.model.vo.article.vo.ArticleDetailVO;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.security.permission.RequiresLogin;
import top.harrylei.forum.service.article.converted.ArticleStructMapper;
import top.harrylei.forum.service.article.service.ArticleService;

/**
 * 文章相关模块
 *
 * @author harry
 */
@Tag(name = "文章相关模块", description = "提供文章的基础查询")
@Slf4j
@RestController
@RequestMapping("/api/v1/article")
@RequiredArgsConstructor
@Validated
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleStructMapper articleStructMapper;

    /**
     * 用户新建文章
     *
     * @param articlePostReq 文章信息请求
     * @return 新建文章ID
     */
    @RequiresLogin
    @Operation(summary = "新建文章", description = "用户新建文章（支持草稿/提交审核）")
    @PostMapping
    public ResVO<Long> create(@Valid @RequestBody ArticlePostReq articlePostReq) {
        ArticleDTO articleDTO = articleStructMapper.toDTO(articlePostReq);
        articleDTO.setUserId(ReqInfoContext.getContext().getUserId());
        Long articleId = articleService.saveArticle(articleDTO);
        return ResVO.ok(articleId);
    }

    /**
     * 编辑文章
     *
     * @param articleUpdateReq 文章更新请求
     * @return 文章VO
     */
    @RequiresLogin
    @Operation(summary = "编辑文章", description = "用户编辑文章")
    @PutMapping
    public ResVO<ArticleVO> update(@Valid @RequestBody ArticleUpdateReq articleUpdateReq) {
        ArticleDTO articleDTO = articleStructMapper.toDTO(articleUpdateReq);
        ArticleVO article = articleService.updateArticle(articleDTO);
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
        articleService.deleteArticle(articleId);
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
        articleService.restoreArticle(articleId);
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
        ArticleDetailVO vo = articleService.getArticleDetail(articleId);
        return ResVO.ok(vo);
    }

    /**
     * 修改状态
     *
     * @param status 修改状态
     * @return 操作结果
     */
    @RequiresLogin
    @Operation(summary = "修改状态", description = "用户修改文章状态")
    @PutMapping("/{articleId}/status")
    public ResVO<Void> updateStatus(@NotNull(message = "文章ID不能为空") @PathVariable Long articleId,
                                    @NotNull(message = "文章状态不能为空") @RequestBody PublishStatusEnum status) {
        articleService.updateArticleStatus(articleId, status);
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
        PageVO<ArticleVO> page = articleService.pageQuery(queryParam);
        return ResVO.ok(page);
    }
}
