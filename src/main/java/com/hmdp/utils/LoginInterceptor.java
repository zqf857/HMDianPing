package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpStatus;
import com.hmdp.constant.LoginConstant;
import com.hmdp.constant.RedisConstants;
import com.hmdp.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

        // 1.判断是否拦截 (ThreadLocal)
        if(UserHolder.getUser() == null){
            // 1.1 没有用户, 拦截
            response.setStatus(HTTP_UNAUTHORIZED);
            return false;
        }

        // 2.放行
        return true;
    }
}
