package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.dto.LoginFormDTO;
import com.travel.dto.Result;
import com.travel.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IUserService extends IService<User> {

    /**
     * 发送手机验证码
     * @param phone
     * @param session
     * @return
     */
    Result sendCode(String phone, HttpSession session);

    /**
     * 登录功能
     * @param loginForm
     * @param session
     * @return
     */
    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();
}
