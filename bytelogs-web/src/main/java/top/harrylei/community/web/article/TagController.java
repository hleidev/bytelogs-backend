package top.harrylei.community.web.article;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.api.model.article.vo.TagSimpleVO;
import top.harrylei.community.api.model.base.ResVO;
import top.harrylei.community.core.security.permission.RequiresLogin;
import top.harrylei.community.service.article.service.TagService;

import java.util.List;

/**
 * 标签相关模块
 *
 * @author harry
 */
@Tag(name = "标签相关模块", description = "提供标签的基础查询")
@Slf4j
@RestController
@RequestMapping("/v1/tag")
@RequiredArgsConstructor
@Validated
public class TagController {

    private final TagService tagService;

    /**
     * 标签列表
     *
     * @return 操作结果
     */
    @Operation(summary = "标签列表", description = "用户标签列表查询")
    @GetMapping("/list")
    public ResVO<List<TagSimpleVO>> list() {
        List<TagSimpleVO> result = tagService.listSimpleTags();
        return ResVO.ok(result);
    }

    /**
     * 标签搜索
     *
     * @param keyword 搜索关键词
     * @return 操作结果
     */
    @Operation(summary = "标签搜索", description = "根据关键词搜索标签")
    @RequiresLogin
    @GetMapping("/search")
    public ResVO<List<TagSimpleVO>> search(@NotBlank(message = "关键词不能为空") @RequestParam String keyword) {
        List<TagSimpleVO> result = tagService.searchTags(keyword);
        return ResVO.ok(result);
    }

    /**
     * 创建标签
     *
     * @param tagName 标签名称
     * @return 操作结果
     */
    @Operation(summary = "创建标签", description = "创建新标签")
    @RequiresLogin
    @PostMapping("/create")
    public ResVO<Long> create(@NotBlank(message = "标签名称不能为空") @RequestParam String tagName) {
        Long userId = ReqInfoContext.getContext().getUserId();
        Long tagId = tagService.createIfAbsent(tagName, userId);
        return ResVO.ok(tagId);
    }
}
