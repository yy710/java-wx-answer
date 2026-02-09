package com.yunkesoftware.www.adm.service;

import com.yunkesoftware.www.adm.entity.SignActivity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 签到活动 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-31
 */
public interface SignActivityService extends IService<SignActivity> {

    void addOrModify(SignActivity signActivity);

    void delete(List<String> ids);
}
