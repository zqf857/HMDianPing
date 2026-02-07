package com.hmdp.service.impl;

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
import com.hmdp.utils.RegexUtils;
import com.sun.xml.internal.bind.v2.TODO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Random;

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
            return Result.fail(MessageConstant.PHONE_INVALID);
        }
        // 3.符合,生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4.保存验证码到session
        session.setAttribute("code", code);
        // 5.发送验证码(不做)
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
            return Result.fail(MessageConstant.PHONE_INVALID);
        }

        // 2. 校验验证码
        Object cacheCode = session.getAttribute(LoginConstant.CODE);
        String code = loginForm.getCode();
        if (cacheCode == null || !code.equals(cacheCode)){
            // 验证码不一致,返回错误
            return Result.fail(MessageConstant.CODE_VERIFICATION_FAILED);
        }

        // 3. 验证码一致, 根据手机号查询用户 select * from tb_user where phone = #{phone}
        User user = query().eq(LoginConstant.PHONE, loginForm.getPhone()).one();
        if(user == null){
            // 4. 创建用户
            user = createUserWithPhone(loginForm.getPhone());
        }

        // 5. 保存用户到session
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        session.setAttribute(LoginConstant.USER, userDTO);
        return Result.ok();
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
