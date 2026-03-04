package com.yunkesoftware.www.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * [Sa-Token 权限认证] 配置类
 *
 * @author kong
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    //     注册Sa-Token的注解拦截器，打开注解式鉴权功能
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册注解拦截器，并排除不需要注解鉴权的接口地址 (与登录拦截器无关)
        SaInterceptor saInterceptor = new SaInterceptor((handler) -> {
            // 根据路由划分模块，不同模块不同鉴权
            SaRouter.match("/sys/**", r -> StpUtil.checkLogin());
        });
        registry.addInterceptor(saInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/sys/sysUser/logout", "/sys/sysUser/login");
    }

    @PostConstruct
    public void rewriteSaStrategy() {
        // 重写Sa-Token的注解处理器，增加注解合并功能
        SaAnnotationStrategy.instance.getAnnotation = AnnotatedElementUtils::getMergedAnnotation;
    }
}