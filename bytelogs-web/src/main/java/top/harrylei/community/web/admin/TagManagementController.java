package top.harrylei.community.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.model.article.dto.TagDTO;
import top.harrylei.community.api.model.article.req.TagReq;
import top.harrylei.community.api.model.article.vo.TagVO;
import top.harrylei.community.api.model.base.Result;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.api.model.page.param.TagQueryParam;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.security.permission.RequiresAdmin;
import top.harrylei.community.core.util.PageUtils;
import top.harrylei.community.service.article.converted.TagStructMapper;
import top.harrylei.community.service.article.service.TagService;

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
    public Result<Void> create(@Valid @RequestBody TagReq tagReq) {
        TagDTO tag = tagStructMapper.toDTO(tagReq);
        tag.setCreatorId(ReqInfoContext.getContext().getUserId());
        tagService.save(tag);
        return Result.success();
    }

    /**
     * 标签分页查询
     *
     * @param queryParam 标签及筛选参数
     * @return 标签列表
     */
    @Operation(summary = "分页查询", description = "支持按名称、类型、时间等多条件分页查询")
    @GetMapping("/page")
    public Result<PageVO<TagVO>> page(TagQueryParam queryParam) {
        PageVO<TagDTO> dtoPage = tagService.pageQuery(queryParam, false);
        return Result.success(PageUtils.map(dtoPage, tagStructMapper::toVO));
    }

    /**
     * 删除标签
     *
     * @param tagId 标签ID
     * @return 操作结果
     */
    @Operation(summary = "删除标签", description = "后台删除标签")
    @DeleteMapping("/{tagId}")
    public Result<Void> delete(@NotNull(message = "标签ID为空") @PathVariable Long tagId) {
        tagService.updateDelete(tagId, DeleteStatusEnum.DELETED);
        return Result.success();
    }

    /**
     * 恢复标签
     *
     * @param tagId 标签ID
     * @return 操作结果
     */
    @Operation(summary = "恢复标签", description = "后台恢复标签")
    @PutMapping("/{tagId}/restore")
    public Result<Void> restore(@NotNull(message = "标签ID为空") @PathVariable Long tagId) {
        tagService.updateDelete(tagId, DeleteStatusEnum.NOT_DELETED);
        return Result.success();
    }

    /**
     * 已删标签分页查询
     *
     * @param queryParam 查询参数
     * @return 已删除标签分页列表
     */
    @Operation(summary = "已删标签分页查询", description = "支持分页查看已删除的标签")
    @GetMapping("/deleted")
    public Result<PageVO<TagVO>> pageDeleted(TagQueryParam queryParam) {
        // 为了分页查询已删除标签，创建一个只有分页信息的查询参数
        TagQueryParam deletedQuery = new TagQueryParam();

        PageVO<TagDTO> deletedTags = tagService.pageQuery(deletedQuery, true);
        return Result.success(PageUtils.map(deletedTags, tagStructMapper::toVO));
    }
}
