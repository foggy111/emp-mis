package com.lab.emp.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器。
 * <p>
 * 任务 3 先提供可编译的实现并完成注册，请求暂时放行，方便先打通 DispatcherServlet。
 * 任务 6 改为：会话中没有 {@code loginUser} 时返回 JSON {@code {"code":401,"message":"未登录"}}。
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return true;
    }
}
