package com.yunkesoftware.www.adm.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.SystemClock;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.yunkesoftware.www.adm.entity.SysLogInfo;
import com.yunkesoftware.www.adm.entity.SysUser;
import com.yunkesoftware.www.adm.service.SysLogInfoService;
import com.yunkesoftware.www.adm.service.SysUserService;
import com.yunkesoftware.www.annotation.YunkeSysLog;
import com.yunkesoftware.www.constant.SysLog;
import com.yunkesoftware.www.utils.IPHelper;
import com.yunkesoftware.www.utils.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class SysLogAspect {
    @Resource
    private SysLogInfoService sysLogInfoService;
    @Resource
    private SysUserService sysUserService;

    private static Logger logger = LoggerFactory.getLogger(SysLogAspect.class);

    @Around(value = "@annotation(YunkeSysLog)")
    public Object around(ProceedingJoinPoint joinPoint, YunkeSysLog YunkeSysLog) throws Throwable {
        long beginTime = SystemClock.now();
        Object result = joinPoint.proceed();
        //执行时长(毫秒)
        long time = SystemClock.now() - beginTime;

        SysLogInfo sysLogEntity = new SysLogInfo();
        if (YunkeSysLog != null) {
            //注解上的描述
            sysLogEntity.setOperation(YunkeSysLog.value());
            sysLogEntity.setType(YunkeSysLog.type());
        }

        //请求的方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        sysLogEntity.setMethod((className + "." + methodName + "()"));

        //请求的参数
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            String params = JSON.toJSONString(args[0]);
            sysLogEntity.setParams(params);
        }


//        //设置IP地址
        sysLogEntity.setIp(IPHelper.getIpAddr());

        //用户名
        if (StringUtils.equals(SysLog.adm_SYS_LOG, YunkeSysLog.type())) {
            SysUser sysUser = sysUserService.getById(StpUtil.getLoginIdAsString());
            if (ObjectUtil.isNotNull(sysUser)){
                sysLogEntity.setUserId(sysUser.getId());
                sysLogEntity.setUsername(sysUser.getUsername());
            }


        }
        sysLogEntity.setTime(time);
        sysLogEntity.setCreateTime(LocalDateTime.now());

//        保存系统日志
        sysLogInfoService.save(sysLogEntity);

        logger.info("执行aop 成功");
        //执行方法
        return result;
    }

}
