package top.harrylei.forum.web.admin;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.article.CategoryCreateReq;
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
    public ResVO<Void> create(@Valid @RequestBody CategoryCreateReq req) {
        categoryManagementService.save(req);
        return ResVO.ok();
    }
}
