package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Mvc 配置类
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private RefreshTokenInterceptor refreshTokenInterceptor;

    @Resource
    private LoginInterceptor loginInterceptor;

    /**
     * 注册用户拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /**
         * 全局拦截器
         * 只刷新请求中 有效token的有效期
         * 所有请求都放行
         */
        registry.addInterceptor(refreshTokenInterceptor)
                .order(0)
                .addPathPatterns("/**");

        /**
         * 用户拦截器
         * 有用户信息才放行
         */
        registry.addInterceptor(loginInterceptor)
                .order(1)
                .excludePathPatterns(
                        "/shop/**",
                        "/voucher/**",
                        "/shop-type/**",
                        "/upload/**",
                        "/blog/hot",
                        "/user/code",
                        "/user/login"
                );
    }
}
