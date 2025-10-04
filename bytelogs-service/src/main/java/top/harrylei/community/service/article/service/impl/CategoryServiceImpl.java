package top.harrylei.community.service.article.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.harrylei.community.api.enums.response.ResultCode;
import top.harrylei.community.api.enums.common.DeleteStatusEnum;
import top.harrylei.community.api.model.article.dto.CategoryDTO;
import top.harrylei.community.api.model.article.req.CategoryReq;
import top.harrylei.community.api.model.article.dto.CategorySimpleDTO;
import top.harrylei.community.api.model.page.PageVO;
import top.harrylei.community.api.model.page.param.CategoryQueryParam;
import top.harrylei.community.core.context.ReqInfoContext;
import top.harrylei.community.core.util.PageUtils;
import top.harrylei.community.service.article.converted.CategoryStructMapper;
import top.harrylei.community.service.article.repository.dao.CategoryDAO;
import top.harrylei.community.service.article.repository.entity.CategoryDO;
import top.harrylei.community.service.article.service.CategoryService;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 分类服务层
 *
 * @author harry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDAO categoryDAO;
    private final CategoryStructMapper categoryStructMapper;

    /**
     * 新建分类
     *
     * @param req 新建分类的请求参数
     */
    @Override
    public void save(CategoryReq req) {
        if (req == null || req.getCategoryName() == null || req.getCategoryName().trim().isEmpty()) {
            ResultCode.INVALID_PARAMETER.throwException("分类请求参数不能为空");
        }

        CategoryDO category = new CategoryDO()
                .setCategoryName(req.getCategoryName())
                .setSort(req.getSort());

        try {
            categoryDAO.save(category);
            log.info("新建分类成功 category={}", category.getCategoryName());
        } catch (Exception e) {
            ResultCode.INTERNAL_ERROR.throwException();
        }
    }

    /**
     * 更新分类
     *
     * @param categoryDTO 分类传输对象
     * @return 新的分类传输对象
     */
    @Override
    public CategoryDTO update(CategoryDTO categoryDTO) {
        if (categoryDTO == null || categoryDTO.getId() == null) {
            ResultCode.INVALID_PARAMETER.throwException("分类更新参数不能为空");
        }

        CategoryDO category = categoryDAO.getByCategoryId(categoryDTO.getId());
        if (category == null) {
            ResultCode.CATEGORY_NOT_EXISTS.throwException();
        }

        // 手动更新可编辑字段，保持ID和审计字段不变
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setSort(categoryDTO.getSort());

        try {
            categoryDAO.updateById(category);
            return categoryStructMapper.toDTO(category);
        } catch (Exception e) {
            ResultCode.INTERNAL_ERROR.throwException();
            return null;
        }
    }

    /**
     * 分类分页查询
     *
     * @param queryParam 分页及筛选参数
     * @return 分页分类列表
     */
    @Override
    public PageVO<CategoryDTO> pageQuery(CategoryQueryParam queryParam) {
        if (queryParam == null) {
            ResultCode.INVALID_PARAMETER.throwException("分页请求参数不能为空");
        }
        // 分页查询
        IPage<CategoryDO> page = PageUtils.of(queryParam);
        IPage<CategoryDO> result = categoryDAO.pageQuery(queryParam, page);

        // 转换为DTO并构建返回结果
        return PageUtils.from(result, categoryStructMapper::toDTO);
    }


    /**
     * 更新删除状态
     *
     * @param categoryId 分类ID
     * @param status     删除状态
     */
    @Override
    public void updateDeleted(Long categoryId, DeleteStatusEnum status) {
        if (categoryId == null) {
            ResultCode.INVALID_PARAMETER.throwException("分类ID不能为空");
        }
        if (status == null) {
            ResultCode.INVALID_PARAMETER.throwException("删除状态不能为空");
        }

        CategoryDO category = categoryDAO.getById(categoryId);
        if (category == null) {
            ResultCode.CATEGORY_NOT_EXISTS.throwException();
        }

        if (Objects.equals(status, category.getDeleted())) {
            log.warn("分类删除状态未变更，无需更新");
            return;
        }

        Long operatorId = ReqInfoContext.getContext().getUserId();

        try {
            category.setDeleted(status);
            categoryDAO.updateById(category);
            log.info("更新分类删除状态成功 category={} operatorId={}", category.getCategoryName(), operatorId);
        } catch (Exception e) {
            ResultCode.INTERNAL_ERROR.throwException();
        }
    }

    /**
     * 分类列表
     *
     * @param deleted 是否查询已删除分类，true查询已删除，false查询未删除
     * @return 分类列表
     */
    @Override
    public List<CategoryDTO> listCategory(boolean deleted) {
        List<CategoryDO> category = categoryDAO.listCategory(deleted);

        return category.stream()
                .map(categoryStructMapper::toDTO)
                .sorted(Comparator.comparingInt(CategoryDTO::getSort).reversed())
                .toList();
    }

    /**
     * 根据分类ID获取分类简单对象
     *
     * @param categoryId 分类ID
     * @return 分类简单对象
     */
    @Override
    public CategorySimpleDTO getSimpleCategoryById(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        CategoryDO category = categoryDAO.getByCategoryId(categoryId);
        if (category == null) {
            return null;
        }
        return categoryStructMapper.toSimpleVO(category);
    }
}
