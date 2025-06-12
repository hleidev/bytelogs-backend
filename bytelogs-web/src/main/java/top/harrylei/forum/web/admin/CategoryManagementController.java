package top.harrylei.forum.web.admin;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.article.dto.CategoryDTO;
import top.harrylei.forum.api.model.vo.article.req.CategoryReq;
import top.harrylei.forum.api.model.vo.article.vo.CategoryDetailVO;
import top.harrylei.forum.api.model.vo.page.PageHelper;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.CategoryQueryParam;
import top.harrylei.forum.core.security.permission.RequiresAdmin;
import top.harrylei.forum.service.article.converted.CategoryStructMapper;
import top.harrylei.forum.service.article.service.CategoryManagementService;

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
    private final CategoryStructMapper categoryStructMapper;

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

    /**
     * 分类分页查询
     *
     * @param queryParam 分页及筛选参数
     * @return 分页分类列表
     */
    @Operation(summary = "分页查询", description = "支持按名称、状态、时间等多条件分页查询")
    @GetMapping("/list")
    public ResVO<PageVO<CategoryDetailVO>> list(CategoryQueryParam queryParam) {
        PageVO<CategoryDTO> page = categoryManagementService.list(queryParam);
        return ResVO.ok(PageHelper.map(page, categoryStructMapper::toAdminVO));
    }

    /**
     * 修改分类状态
     *
     * @param categoryId 分类ID
     * @param status 新状态
     * @return 操作结果
     */
    @Operation(summary = "修改状态", description = "发布/未发布状态变更")
    @PutMapping("/{categoryId}/status")
    public ResVO<Void> updateStatus(@NotNull(message = "分类ID为空") @PathVariable Long categoryId,
        @NotNull(message = "状态为空") @RequestBody PublishStatusEnum status) {
        categoryManagementService.updateStatus(categoryId, status);
        return ResVO.ok();
    }

    /**
     * 删除分类
     * 
     * @param categoryId 分类ID
     * @return 操作结果
     */
    @Operation(summary = "删除分类", description = "根据分类ID删除分类")
    @DeleteMapping("/{categoryId}")
    public ResVO<Void> delete(@NotNull(message = "分类ID为空") @PathVariable Long categoryId) {
        categoryManagementService.delete(categoryId);
        return ResVO.ok();
    }

    /**
     * 恢复分类
     *
     * @param categoryId 分类ID
     * @return 操作结果
     */
    @Operation(summary = "恢复分类", description = "根据分类ID恢复分类")
    @PutMapping("/{categoryId}/restore")
    public ResVO<Void> restore(@NotNull(message = "分类ID为空") @PathVariable Long categoryId) {
        categoryManagementService.restore(categoryId);
        return ResVO.ok();
    }

    /**
     * 已删分类
     *
     * @return 操作结果
     */
    @Operation(summary = "已删分类", description = "查看所有已删除的分类")
    @GetMapping("/deleted")
    public ResVO<List<CategoryDetailVO>> listDeleted() {
        List<CategoryDTO> categories = categoryManagementService.listDeleted();
        List<CategoryDetailVO> list = categories.stream().map(categoryStructMapper::toAdminVO).toList();
        return ResVO.ok(list);
    }
}
