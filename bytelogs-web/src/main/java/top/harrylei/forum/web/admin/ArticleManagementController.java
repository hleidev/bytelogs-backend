package top.harrylei.forum.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.article.req.ArticleQueryParam;
import top.harrylei.forum.api.model.vo.article.vo.ArticleVO;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.core.security.permission.RequiresAdmin;
import top.harrylei.forum.service.article.service.ArticleService;

/**
 * 文章管理模块
 *
 * @author Harry
 */
@Tag(name = "文章管理模块", description = "提供文章后台管理接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
@RequiresAdmin
@Validated
public class ArticleManagementController {

    private final ArticleService articleService;

    /**
     * 文章分页查询
     *
     * @param queryParam 查询参数
     * @return 分页查询结果
     */
    @Operation(summary = "分页查询", description = "管理端分页查询")
    @GetMapping("/page")
    public ResVO<PageVO<ArticleVO>> pageQuery(@Valid ArticleQueryParam queryParam) {
        PageVO<ArticleVO> page = articleService.pageQuery(queryParam);
        return ResVO.ok(page);
    }
}
