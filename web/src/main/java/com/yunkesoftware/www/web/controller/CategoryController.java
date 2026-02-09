package com.yunkesoftware.www.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.CategoryService;
import com.yunkesoftware.www.web.entity.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-02-02
 */
@Tag(name = "分类信息")
@RestController
@RequestMapping("/wx/category")
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    @Operation(summary = "获取全部列表")
    @PostMapping("/list")
    public CommonResult<List<Category>> list() {
        return CommonResult.success(categoryService.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, true)
                .orderByAsc(Category::getSeq)));
    }
}
