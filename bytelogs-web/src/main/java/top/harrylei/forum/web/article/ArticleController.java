package top.harrylei.forum.web.article;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.article.dto.ArticleDTO;
import top.harrylei.forum.api.model.vo.article.req.ArticlePostReq;
import top.harrylei.forum.api.model.vo.article.req.ArticleUpdateReq;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.security.permission.RequiresLogin;
import top.harrylei.forum.service.article.converted.ArticleStructMapper;
import top.harrylei.forum.service.article.service.ArticleService;

/**
 * 文章相关模块
 */
@Tag(name = "文章相关模块", description = "提供文章的基础查询")
@Slf4j
@RestController
@RequestMapping("/api/v1/article")
@RequiredArgsConstructor
@RequiresLogin
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
    @Operation(summary = "编辑文章", description = "用户编辑文章")
    @PutMapping
    public ResVO<ArticleVO> update(@Valid @RequestBody ArticleUpdateReq articleUpdateReq) {
        ArticleDTO articleDTO = articleStructMapper.toDTO(articleUpdateReq);
        ArticleVO article = articleService.updateArticle(articleDTO, ReqInfoContext.getContext().getUserId());
        return ResVO.ok(article);
    }

    /**
     * 删除文章
     * 
     * @param articleId 文章ID
     * @return 操作结果
     */
    @Operation(summary = "删除文章", description = "用户删除文章")
    @DeleteMapping("/{articleId}")
    public ResVO<Void> delete(@PathVariable Long articleId) {
        articleService.deleteArticle(articleId, ReqInfoContext.getContext().getUserId());
        return ResVO.ok();
    }
}
