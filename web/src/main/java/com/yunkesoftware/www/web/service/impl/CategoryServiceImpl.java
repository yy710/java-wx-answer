package com.yunkesoftware.www.web.service.impl;

import com.yunkesoftware.www.web.entity.Category;
import com.yunkesoftware.www.web.mapper.CategoryMapper;
import com.yunkesoftware.www.web.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 分类信息 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-02-02
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

}
