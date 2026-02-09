package com.yunkesoftware.www.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.web.entity.Area;


import java.util.List;

public interface AreaService extends IService<Area> {
    List<Area> listByPid(String pid);

    List<Area> listTree();

}
