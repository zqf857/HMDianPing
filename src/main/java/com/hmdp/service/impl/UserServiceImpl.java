package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.constant.LoginConstant;
import com.hmdp.constant.MessageConstant;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.constant.RedisConstants;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.nio.file.CopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.constant.LoginConstant.PHONE;
import static com.hmdp.constant.MessageConstant.CODE_VERIFICATION_FAILED;
import static com.hmdp.constant.MessageConstant.PHONE_INVALID;
import static com.hmdp.constant.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送并保存验证码
     *
     * @param phone
     * @param session
     * @return
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)){
            // 2. 不符合,返回错误
            return Result.fail(PHONE_INVALID);
        }

        // 3.符合,生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 4.保存验证码到redis
        // login;code + phone, code, ttl, unit
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone,
                code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 5.发送验证码 (不实现)
        log.debug("发送短信验证码成功,验证码: {}", code);

        // 返回ok
        return Result.ok();
    }

    /**
     * 用户登录
     *
     * @param loginForm
     * @param session
     * @return
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1. 校验手机号
        if (RegexUtils.isPhoneInvalid(loginForm.getPhone())){
            // 不符合,返回错误
            return Result.fail(PHONE_INVALID);
        }

        // 2. 校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + loginForm.getPhone());
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)){
            // 验证码不一致,返回错误
            return Result.fail(CODE_VERIFICATION_FAILED);
        }

        // 3. 验证码一致, 根据手机号查询用户 select * from tb_user where phone = #{phone}
        User user = query().eq(PHONE, loginForm.getPhone()).one();
        if(user == null){
            // 4. 创建用户
            user = createUserWithPhone(loginForm.getPhone());
        }

        // 5.保存用户到redis
        // 5.1. 随机生成token,作为登录令牌
        String token = UUID.randomUUID().toString(true);
        // 5.2. 将user对象转为hashMap
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create().
                        setIgnoreNullValue(true).
                        setFieldValueEditor((key, value) -> value.toString()));

        // 5.3. 存储
        String tokenKey = LOGIN_USER_KEY + token;   // login:user: + token
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 5.4. 配置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.SECONDS);

        // 6.返回给前端token
        return Result.ok(token);
    }

    /**
     * 根据手机号创建新用户
     * @param phone
     * @return
     */
    private User createUserWithPhone(String phone) {
        // 1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(LoginConstant.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));

        // 2.保存用户到 tb_user
        save(user);
        return user;
    }
}
