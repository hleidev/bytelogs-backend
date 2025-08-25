package top.harrylei.forum.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.harrylei.forum.api.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.base.ResVO;
import top.harrylei.forum.api.model.article.dto.CategoryDTO;
import top.harrylei.forum.api.model.article.req.CategoryReq;
import top.harrylei.forum.api.model.article.vo.CategoryVO;
import top.harrylei.forum.api.model.page.PageVO;
import top.harrylei.forum.api.model.page.param.CategoryQueryParam;
import top.harrylei.forum.core.security.permission.RequiresAdmin;
import top.harrylei.forum.core.util.PageUtils;
import top.harrylei.forum.service.article.converted.CategoryStructMapper;
import top.harrylei.forum.service.article.service.CategoryService;

import java.util.List;

/**
 * 分类管理模块
 *
 * @author harry
 */
@Tag(name = "分类管理模块", description = "提供分类后台管理接口")
@Slf4j
@RestController
@RequestMapping("/v1/admin/category")
@RequiresAdmin
@RequiredArgsConstructor
@Validated
public class CategoryManagementController {

    private final CategoryStructMapper categoryStructMapper;
    private final CategoryService categoryService;

    /**
     * 新建分类
     *
     * @param req 新建分类的请求参数
     * @return 操作结果
     */
    @Operation(summary = "新建分类", description = "后台管理端新建分类")
    @PostMapping
    public ResVO<Void> create(@Valid @RequestBody CategoryReq req) {
        categoryService.save(req);
        return ResVO.ok();
    }

    /**
     * 修改分类
     *
     * @param categoryId 分类ID
     * @param req        修改参数
     * @return 操作结果
     */
    @Operation(summary = "修改分类", description = "修改现有分类信息")
    @PutMapping("/{categoryId}")
    public ResVO<CategoryVO> update(@NotNull(message = "分类ID为空") @PathVariable Long categoryId,
                                    @Valid @RequestBody CategoryReq req) {
        CategoryDTO categoryDTO = categoryStructMapper.toDTO(req);
        categoryDTO.setId(categoryId);
        CategoryDTO updatedDTO = categoryService.update(categoryDTO);
        return ResVO.ok(categoryStructMapper.toVO(updatedDTO));
    }

    /**
     * 分类分页查询
     *
     * @param queryParam 分页及筛选参数
     * @return 分页分类列表
     */
    @Operation(summary = "分页查询", description = "支持按名称、状态、时间等多条件分页查询")
    @GetMapping("/page")
    public ResVO<PageVO<CategoryVO>> page(CategoryQueryParam queryParam) {
        PageVO<CategoryDTO> page = categoryService.pageQuery(queryParam);
        return ResVO.ok(PageUtils.map(page, categoryStructMapper::toVO));
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
        categoryService.updateDeleted(categoryId, YesOrNoEnum.YES);
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
        categoryService.updateDeleted(categoryId, YesOrNoEnum.NO);
        return ResVO.ok();
    }

    /**
     * 已删分类
     *
     * @return 操作结果
     */
    @Operation(summary = "已删分类", description = "查看所有已删除的分类")
    @GetMapping("/deleted")
    public ResVO<List<CategoryVO>> listDeleted() {
        List<CategoryDTO> categories = categoryService.listCategory(true);
        List<CategoryVO> list = categories.stream().map(categoryStructMapper::toVO).toList();
        return ResVO.ok(list);
    }
}
