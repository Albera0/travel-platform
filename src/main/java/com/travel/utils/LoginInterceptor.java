package com.travel.utils;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // p判断是否需要取拦截（ThreadLocal中是否有用户）
        if (UserHolder.getUser() == null) {
            //为空则拦截，设置状态码
            response.setStatus(401);
            return false;
        }

        //有用户放行
        return true;
    }

}
