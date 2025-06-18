package top.harrylei.forum.web.article;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.vo.ResVO;
import top.harrylei.forum.api.model.vo.article.vo.TagSimpleVO;
import top.harrylei.forum.service.article.converted.TagStructMapper;
import top.harrylei.forum.service.article.service.TagService;

/**
 * 标签相关模块
 */
@Tag(name = "标签相关模块", description = "提供标签的基础查询")
@Slf4j
@RestController
@RequestMapping("/api/v1/tag")
@RequiredArgsConstructor
@Validated
public class TagController {

    private final TagService tagService;
    private final TagStructMapper tagStructMapper;

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
}
