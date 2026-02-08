package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpStatus;
import com.hmdp.constant.LoginConstant;
import com.hmdp.constant.RedisConstants;
import com.hmdp.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.hutool.http.HttpStatus.*;
import static com.hmdp.constant.LoginConstant.*;
import static com.hmdp.constant.RedisConstants.*;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取请求头中的token
        String token = request.getHeader(HEADERS_TOKEN_KEY);
        if (token == null || token.isEmpty()){
            // 不存在token 返回401
            response.setStatus(HTTP_UNAUTHORIZED);
            return false;
        }

        // 2.根据token获取redis中的用户
        String tokenKey = LOGIN_USER_KEY + token;   // login:token: + token
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);

        // 3.判断用户是否存在
        if(userMap.isEmpty()){
            response.setStatus(HTTP_UNAUTHORIZED);
            return false;
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
