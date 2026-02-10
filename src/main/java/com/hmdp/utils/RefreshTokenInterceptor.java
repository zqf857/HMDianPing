package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.constant.LoginConstant.HEADERS_TOKEN_KEY;
import static com.hmdp.constant.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.constant.RedisConstants.LOGIN_USER_TTL;

@Component
public class RefreshTokenInterceptor implements HandlerInterceptor {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 不存在token 或者token无效 直接放行到下一拦截器
     * 只有存在有效token才刷新token有效期
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取请求头中的token
        String token = request.getHeader(HEADERS_TOKEN_KEY);
        if (token == null || token.isEmpty()){
            // 不存在token 放行
            return true;
        }

        // 2.根据token获取redis中的用户
        String tokenKey = LOGIN_USER_KEY + token;   // login:token: + token
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);

        // 3.判断用户是否存在
        if(userMap.isEmpty()){
            // 不存在用户 放行
            return true;
        }

        // 4.将查询到的hash转化为UserDTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        // 5.存在,保存信息到ThreadLocal
        UserHolder.saveUser(userDTO);

        // 6.刷新token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.SECONDS);

        // 7.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户
        UserHolder.removeUser();
    }
}
