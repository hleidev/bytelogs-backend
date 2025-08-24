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
import top.harrylei.forum.api.model.article.dto.TagDTO;
import top.harrylei.forum.api.model.article.req.TagReq;
import top.harrylei.forum.api.model.article.vo.TagVO;
import top.harrylei.forum.api.model.base.ResVO;
import top.harrylei.forum.api.model.page.PageVO;
import top.harrylei.forum.api.model.page.param.TagQueryParam;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.security.permission.RequiresAdmin;
import top.harrylei.forum.core.util.PageUtils;
import top.harrylei.forum.service.article.converted.TagStructMapper;
import top.harrylei.forum.service.article.service.TagService;

/**
 * 标签管理模块
 *
 * @author harry
 */
@Tag(name = "标签管理模块", description = "提供分类后台管理接口")
@Slf4j
@RestController
@RequestMapping("/v1/admin/tag")
@RequiresAdmin
@RequiredArgsConstructor
@Validated
public class TagManagementController {

    private final TagService tagService;
    private final TagStructMapper tagStructMapper;

    /**
     * 新建标签
     *
     * @param tagReq 参数
     * @return 操作结果
     */
    @Operation(summary = "新建标签", description = "后台新建标签")
    @PostMapping
    public ResVO<Void> create(@Valid @RequestBody TagReq tagReq) {
        TagDTO tag = tagStructMapper.toDTO(tagReq);
        tag.setCreatorId(ReqInfoContext.getContext().getUserId());
        tagService.save(tag);
        return ResVO.ok();
    }

    /**
     * 标签分页查询
     *
     * @param queryParam 标签及筛选参数
     * @return 标签列表
     */
    @Operation(summary = "分页查询", description = "支持按名称、类型、时间等多条件分页查询")
    @GetMapping("/page")
    public ResVO<PageVO<TagVO>> page(TagQueryParam queryParam) {
        PageVO<TagDTO> dtoPage = tagService.pageQuery(queryParam);
        return ResVO.ok(PageUtils.map(dtoPage, tagStructMapper::toVO));
    }

    /**
     * 删除标签
     *
     * @param tagId 标签ID
     * @return 操作结果
     */
    @Operation(summary = "删除标签", description = "后台删除标签")
    @DeleteMapping("/{tagId}")
    public ResVO<Void> delete(@NotNull(message = "标签ID为空") @PathVariable Long tagId) {
        tagService.updateDelete(tagId, YesOrNoEnum.YES);
        return ResVO.ok();
    }

    /**
     * 恢复标签
     *
     * @param tagId 标签ID
     * @return 操作结果
     */
    @Operation(summary = "恢复标签", description = "后台恢复标签")
    @PutMapping("/{tagId}/restore")
    public ResVO<Void> restore(@NotNull(message = "标签ID为空") @PathVariable Long tagId) {
        tagService.updateDelete(tagId, YesOrNoEnum.NO);
        return ResVO.ok();
    }

    /**
     * 已删标签
     *
     * @return 已经删除的标签详细信息列表
     */
    @Operation(summary = "已删标签", description = "后台查看已删除的标签")
    @GetMapping("/deleted")
    public ResVO<PageVO<TagVO>> pageDeleted() {
        PageVO<TagDTO> deletedTags = tagService.pageQuery(new TagQueryParam(), true);
        return ResVO.ok(PageUtils.map(deletedTags, tagStructMapper::toVO));
    }
}
