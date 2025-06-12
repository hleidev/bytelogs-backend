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
import top.harrylei.forum.api.model.vo.article.req.TagReq;
import top.harrylei.forum.core.security.permission.RequiresAdmin;
import top.harrylei.forum.service.article.service.TagManagementService;

/**
 * 标签管理模块
 */
@Tag(name = "标签管理模块", description = "提供分类后台管理接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/tag")
@RequiresAdmin
@RequiredArgsConstructor
@Validated
public class TagManagementController {

    private final TagManagementService tagManagementService;

    /**
     * 新建标签
     * 
     * @param tagReq 参数
     * @return 操作结果
     */
    @Operation(summary = "新建标签", description = "后台新建标签")
    @PostMapping
    public ResVO<Void> create(@Valid @RequestBody TagReq tagReq) {
        tagManagementService.save(tagReq);
        return ResVO.ok();
    }
}
