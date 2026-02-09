package com.yunkesoftware.www.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.web.entity.User;
import com.yunkesoftware.www.web.vo.LoginVo;
import com.yunkesoftware.www.web.vo.UserVo;

import java.util.Map;

/**
 * <p>
 * 客户信息 服务类
 * </p>
 *
 * @author cuiyq
 * @since 2024-10-24
 */
public interface UserService extends IService<User> {


    UserVo info(String userId);

    void updateWithClear(UserVo userVo);

    Map<String, Object> wxAuth(LoginVo loginVo);

}
