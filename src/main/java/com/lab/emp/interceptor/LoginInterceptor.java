package com.lab.emp.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 未登录时直接写 JSON，不进入 Controller。
 * 指导书示例里的字符串写成了 {"code":401,...}，在 Java 源码中会提前结束字符串，这里改为合法字面量。
 */
public class LoginInterceptor implements HandlerInterceptor {

    static final String UNAUTHORIZED_JSON = "{\"code\":401,\"message\":\"未登录\"}";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        HttpSession session = request.getSession(false);
        Object user = session == null ? null : session.getAttribute("loginUser");
        if (user == null) {
            response.setStatus(401);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(UNAUTHORIZED_JSON);
            return false;
        }
        return true;
    }
}
