package top.harrylei.forum.service.article.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.harrylei.forum.api.model.enums.article.PublishStatusEnum;
import top.harrylei.forum.api.model.enums.StatusEnum;
import top.harrylei.forum.api.model.enums.YesOrNoEnum;
import top.harrylei.forum.api.model.vo.article.dto.CategoryDTO;
import top.harrylei.forum.api.model.vo.article.req.CategoryReq;
import top.harrylei.forum.api.model.vo.page.Page;
import top.harrylei.forum.api.model.vo.page.PageHelper;
import top.harrylei.forum.api.model.vo.page.PageVO;
import top.harrylei.forum.api.model.vo.page.param.CategoryQueryParam;
import top.harrylei.forum.core.context.ReqInfoContext;
import top.harrylei.forum.core.exception.ExceptionUtil;
import top.harrylei.forum.service.article.converted.CategoryStructMapper;
import top.harrylei.forum.service.article.repository.dao.CategoryDAO;
import top.harrylei.forum.service.article.repository.entity.CategoryDO;
import top.harrylei.forum.service.article.service.CategoryService;

/**
 * 分类服务层
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
        ExceptionUtil.requireNonNull(req, StatusEnum.PARAM_MISSING, "分类请求参数");

        CategoryDO category =
            new CategoryDO().setCategoryName(req.getCategoryName()).setStatus(req.getStatus().getCode()).setSort(req.getSort());

        try {
            categoryDAO.save(category);
            log.info("新建分类成功 category={}", category.getCategoryName());
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.SYSTEM_ERROR, "新建分类失败", e);
        }
    }

    /**
     * 更新分类
     *
     * @param categoryId 分类ID
     * @param req 修改参数
     */
    @Override
    public void update(Long categoryId, CategoryReq req) {
        ExceptionUtil.requireNonNull(categoryId, StatusEnum.PARAM_MISSING, "分类ID");
        ExceptionUtil.requireNonNull(req, StatusEnum.PARAM_MISSING, "分类请求参数");

        CategoryDO category = categoryDAO.getByCategoryId(categoryId);
        ExceptionUtil.requireNonNull(category, StatusEnum.CATEGORY_NOT_EXISTS);

        categoryStructMapper.updateDOFromReq(req, category);

        try {
            categoryDAO.updateById(category);
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.CATEGORY_UPDATE_FAILED, "新建分类失败", e);
        }
    }

    /**
     * 分类分页查询
     *
     * @param queryParam 分页及筛选参数
     * @return 分页分类列表
     */
    @Override
    public PageVO<CategoryDTO> page(CategoryQueryParam queryParam) {
        ExceptionUtil.requireNonNull(queryParam, StatusEnum.PARAM_MISSING, "分页请求参数");
        Page page = PageHelper.createPage(queryParam.getPageNum(), queryParam.getPageSize());

        List<CategoryDO> categoryDOList = categoryDAO.listCategory(queryParam, page.getLimitSql());
        long total = categoryDAO.countCategory(queryParam);

        List<CategoryDTO> categoryList = categoryDOList.stream().map(categoryStructMapper::toDTO).toList();

        return PageHelper.build(categoryList, page.getPageNum(), page.getPageSize(), total);
    }

    /**
     * 更新分类状态
     *
     * @param categoryId 分类ID
     * @param status 新状态
     */
    @Override
    public void updateStatus(Long categoryId, PublishStatusEnum status) {
        ExceptionUtil.requireNonNull(categoryId, StatusEnum.PARAM_MISSING, "分类ID");
        ExceptionUtil.requireNonNull(status, StatusEnum.PARAM_MISSING, "分类状态");

        CategoryDO category = categoryDAO.getByCategoryId(categoryId);
        ExceptionUtil.requireNonNull(category, StatusEnum.CATEGORY_NOT_EXISTS);

        if (Objects.equals(status.getCode(), category.getStatus())) {
            log.warn("分类状态未变更，无需更新");
            return;
        }

        Long operatorId = ReqInfoContext.getContext().getUserId();

        try {
            category.setStatus(status.getCode());
            categoryDAO.updateById(category);
            log.info("更新分类状态成功 category={} status={} operatorId={}", category.getCategoryName(), status.getLabel(),
                operatorId);
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.CATEGORY_UPDATE_FAILED, "更新状态失败", e);
        }
    }

    /**
     * 更新删除状态
     *
     * @param categoryId 分类ID
     * @param status 删除状态
     */
    @Override
    public void updateDeleted(Long categoryId, YesOrNoEnum status) {
        ExceptionUtil.requireNonNull(categoryId, StatusEnum.PARAM_MISSING, "分类ID");
        ExceptionUtil.requireNonNull(status, StatusEnum.PARAM_MISSING, "删除状态");

        CategoryDO category = categoryDAO.getById(categoryId);
        ExceptionUtil.requireNonNull(category, StatusEnum.CATEGORY_NOT_EXISTS);

        if (Objects.equals(status.getCode(), category.getDeleted())) {
            log.warn("分类删除状态未变更，无需更新");
            return;
        }

        Long operatorId = ReqInfoContext.getContext().getUserId();

        try {
            category.setDeleted(status.getCode());
            categoryDAO.updateById(category);
            log.info("更新分类删除状态成功 category={} operatorId={}", category.getCategoryName(), operatorId);
        } catch (Exception e) {
            ExceptionUtil.error(StatusEnum.CATEGORY_UPDATE_FAILED, "更新删除状态失败", e);
        }
    }

    /**
     * 已删分类
     *
     * @return 已删分类列表
     */
    @Override
    public List<CategoryDTO> listDeleted() {
        List<CategoryDO> categoryList = categoryDAO.getDeleted();
        ExceptionUtil.requireNonNull(categoryList, StatusEnum.CATEGORY_NOT_EXISTS);

        return categoryList.stream().map(categoryStructMapper::toDTO).toList();
    }

    /**
     * 分类列表
     *
     * @return 分类列表
     */
    @Override
    public List<CategoryDTO> list() {
        List<CategoryDO> category = categoryDAO.listPublishedAndUndeleted();

        return category.stream()
                .filter(Objects::nonNull)
                .map(categoryStructMapper::toDTO)
                .sorted(Comparator.comparingInt(CategoryDTO::getSort).reversed())
                .toList();
    }
}
