package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.adm.entity.Area;

import java.util.List;

/**
 * <p>
 * 地区管理 服务类
 * </p>
 *
 * @author g
 * @since 2023-12-11
 */
public interface AreaService extends IService<Area> {

    List<Area> getAreaTree();


}
