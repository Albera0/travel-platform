package com.travel.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.dto.LoginFormDTO;
import com.travel.dto.Result;
import com.travel.dto.UserDTO;
import com.travel.entity.User;
import com.travel.mapper.UserMapper;
import com.travel.service.IUserService;
import com.travel.utils.RegexUtils;
import com.travel.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.travel.utils.RedisConstants.*;
import static com.travel.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    public StringRedisTemplate stringRedisTemplate;

    /**
     * 发送手机验证码
     * @param phone
     * @param session
     * @return
     */
    public Result sendCode(String phone, HttpSession session) {
        //1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            //2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }

        //3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);

        //4.保存验证码到redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        //5.发送验证码
        log.debug("发送短信验证码成功，验证码：{}", code);

        //返回
        return Result.ok();
    }

    /**
     * 登录功能
     * @param loginForm
     * @param session
     * @return
     */
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1.校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            //2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }

        //2.校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !code.equals(cacheCode)) {
            //3.不一致，报错
            return Result.fail("验证码错误");
        }

        //4.一致，依据手机号查询用户
        User user = query().eq("phone", phone).one();

        //5.判断用户是否存在
        if (user == null) {
            //6.不存在，创建新用户并保存
            user = createUserWithPhone(phone);
        }

        //7.保存用户信息到redis里
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create().
                        setIgnoreNullValue(true).
                        setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        String tokenKEY = LOGIN_USER_KEY+token;
        stringRedisTemplate.opsForHash().putAll(tokenKEY,  userMap);
        stringRedisTemplate.expire(tokenKEY, LOGIN_USER_TTL, TimeUnit.MINUTES);

        return Result.ok(token);
    }

    @Override
    public Result sign() {
        //获取当前用户
        Long userId = UserHolder.getUser().getId();

        //获取日期
        LocalDateTime now = LocalDateTime.now();

        //拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;

        //获取今天是本月第几天
        int dayOfMonth = now.getDayOfMonth();

        //写入redis
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth-1, true);

        return Result.ok();
    }

    @Override
    public Result signCount() {
        //获取当前用户
        Long userId = UserHolder.getUser().getId();

        //获取日期
        LocalDateTime now = LocalDateTime.now();

        //拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;

        //获取今天是本月第几天
        int dayOfMonth = now.getDayOfMonth();

        //获取本月截至今天为止所有的签到记录，返回十进制数字
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create().
                        get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );
        if (result.isEmpty() || result == null) {
            return Result.ok(0);
        }
        Long num = result.get(0);
        if (num == null || num == 0) {
            return Result.ok(0);
        }

        //循环遍历
        int count = 0;
        while (true){
            //判断bit位是否为1
            if ((num & 1) == 0){
                //如果为零，说明未签到，结束
                break;
            }else {
                //不为零说明已签到，计数器+1
                count ++;
            }
            //把数字右移一位，抛弃最后一个bit位，继续下一个bit位
            num >>>= 1;
        }

        return Result.ok(count);
    }

    private User createUserWithPhone(String phone) {
        //1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX  + RandomUtil.randomString(10));

        //2.保存用户信息
        save(user);

        return user;
    }
}
