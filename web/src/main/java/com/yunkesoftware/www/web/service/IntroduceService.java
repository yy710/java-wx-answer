package com.yunkesoftware.www.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.web.entity.Introduce;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 介绍信息 服务类
 * </p>
 *
 * @author yk
 * @since 2026-03-11
 */
public interface IntroduceService extends IService<Introduce> {

    Page<Introduce> pageByQuery(Introduce introduce);
}
