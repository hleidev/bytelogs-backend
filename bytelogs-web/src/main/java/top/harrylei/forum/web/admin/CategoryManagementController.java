package top.harrylei.forum.web.admin;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.article.CategoryReq;
import top.harrylei.forum.core.security.permission.RequiresAdmin;
import top.harrylei.forum.service.admin.service.CategoryManagementService;

/**
 * 分类管理模块
 */
@Tag(name = "分类管理模块", description = "提供分类后台管理接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/category")
@RequiresAdmin
@RequiredArgsConstructor
@Validated
public class CategoryManagementController {

    private final CategoryManagementService categoryManagementService;

    /**
     * 新建分类
     *
     * @param req 新建分类的请求参数
     * @return 操作结果
     */
    @Operation(summary = "新建分类", description = "后台管理端新建分类")
    @PostMapping
    public ResVO<Void> create(@Valid @RequestBody CategoryReq req) {
        categoryManagementService.save(req);
        return ResVO.ok();
    }

    /**
     * 修改分类
     *
     * @param categoryId 分类ID
     * @param req 修改参数
     * @return 操作结果
     */
    @Operation(summary = "修改分类", description = "修改现有分类信息")
    @PutMapping("/{categoryId}")
    public ResVO<Void> update(@NotNull(message = "分类ID为空") @PathVariable Long categoryId,
        @Valid @RequestBody CategoryReq req) {
        categoryManagementService.update(categoryId, req);
        return ResVO.ok();
    }
}
