package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.CategoryService;
import com.yunkesoftware.www.adm.entity.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-02-02
 */
@Tag(name = "分类信息")
@RestController
@RequestMapping("/sys/category")
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    @Operation(summary = "获取全部列表")
    @PostMapping("/list")
    public CommonResult<List<Category>> list(@RequestBody Category category) {
        return CommonResult.success(categoryService.list(new LambdaQueryWrapper<>(category)));
    }

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<Category>> page(@RequestBody Category category) {
        return CommonResult.success(categoryService.page(new Page<>(category.getPageNum(), category.getPageSize()),
                new LambdaQueryWrapper<Category>()
                        .eq(StringUtils.hasLength(category.getTitle()), Category::getTitle, category.getTitle())
                        .orderByAsc(Category::getSeq)));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<Category> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(categoryService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    public CommonResult<Boolean> addOrModify(@RequestBody Category category) {
        return CommonResult.status(categoryService.saveOrUpdate(category));
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        return CommonResult.status(categoryService.removeByIds(ids));
    }
}
