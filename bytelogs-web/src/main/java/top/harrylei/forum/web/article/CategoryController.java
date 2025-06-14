package top.harrylei.forum.web.article;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.article.dto.CategoryDTO;
import top.harrylei.forum.api.model.vo.article.vo.CategorySimpleVO;
import top.harrylei.forum.service.article.converted.CategoryStructMapper;
import top.harrylei.forum.service.article.service.CategoryService;

/**
 * 分类管理控制器
 */
@Tag(name = "分类相关模块", description = "提供分类的增删改查接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryStructMapper categoryStructMapper;

    /**
     * 分类列表
     *
     * @return 分类列表
     */
    @Operation(summary = "分类列表", description = "返回已排序的分类列表")
    @GetMapping("/list")
    public ResVO<List<CategorySimpleVO>> list() {
        List<CategoryDTO> category = categoryService.list();
        List<CategorySimpleVO> result = category.stream().map(categoryStructMapper::toSimpleVO).toList();
        return ResVO.ok(result);
    }
}