package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
   private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserMapper userMapper;
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1. 使用正则表达式，校验手机号
        if(RegexUtils.isPhoneInvalid((phone))){
            //2.如果不符合返回错误信息
            return Result.fail("手机号格式错误！");
        }
        //3.符合，生成验证码
        String code= RandomUtil.randomNumbers(6);
        //4.保存验证码到 session
        //session.setAttribute("code",code);
        //将验证码保存到redis中
        stringRedisTemplate.opsForValue().set(LOGIN_USER_KEY+phone,code,2, TimeUnit.MINUTES);
        //5. 发送验证码
        log.debug("发送短信验证码成功，验证码：{}",code);
        //返回OK
        return Result.ok();
    }

    /**
     * 登录
     * @param loginForm
     * @param session
     * @return
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1.校验手机号
        String phone=loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            //手机号不符合
            return Result.fail("手机号格式错误");
        }
        //3.校验验证码 获取redis中的验证码，获取用户输入的验证码，然后两者进行比较
        Object cacheCode=stringRedisTemplate.opsForValue().get(LOGIN_USER_KEY+phone);
        String code=loginForm.getCode();
        if(cacheCode==null || !cacheCode.toString().equals(code)){
            //验证码不一致，报错
            return Result.fail("验证码错误");
        }
        //4.一致的话，根据手机号查询用户 使用mybatis-plus进行查询
        User user=query().eq("phone",phone).one();
        //5.判断用户是否存在
        if(user==null){
            //6.用不存在，则创建用户
            user=createUserWithPhone(phone);
        }
        //随机生成token返回给前端作为登录的令牌
        String token= UUID.randomUUID().toString();
        // 将user中的属性拷贝到UserDTO对象当中
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user,userDTO);
        //将UserDTO对象转化为HashMap存储
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create() // 将所有字段都转变成string型
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        //存储
        String tokenKey=LOGIN_USER_KEY+token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        //设置token有效期
        stringRedisTemplate.expire(tokenKey,LOGIN_USER_TTL,TimeUnit.MINUTES);
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        //1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));
        //2.保存用户
        save(user);
        return user;
    }

    /**
     * 根据id查询用户信息
     * @param id
     * @return
     */
    @Override
    public User getUserInfoById(Long id) {
        User user=userMapper.getUserById(id);
        return user;
    }

    /**
     * 用户签到
     * @return
     */
    @Override
    public Result sign() {
        Long userId= UserHolder.getUser().getId();
        //获取当前时间
        LocalDateTime dataTime=LocalDateTime.now();
        //获取年月
        String keySuffix=dataTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        log.info("当前年月：{}",keySuffix);
        //拼接redis的key
        String key= RedisConstants.USER_SIGN_KEY+userId+keySuffix;
        //获取今天是本月的第几天
        int day=dataTime.getDayOfMonth();
        //写入redis中
        stringRedisTemplate.opsForValue().setBit(key,day-1,true);
        return Result.ok();
    }
}
