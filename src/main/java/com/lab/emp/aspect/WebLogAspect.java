package com.lab.emp.aspect;

import com.lab.emp.dto.LoginDTO;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 记录 Controller 入参、耗时和异常。口令字段不写进日志。
 */
@Aspect
@Component
public class WebLogAspect {

    private static final Logger LOGGER = Logger.getLogger(WebLogAspect.class);

    @Pointcut("execution(* com.lab.emp.controller..*(..))")
    public void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String method = joinPoint.getSignature().toShortString();
        LOGGER.info("[REQ] " + method + " 参数：" + sanitize(joinPoint.getArgs()));
        try {
            Object result = joinPoint.proceed();
            LOGGER.info("[RESP] " + method + " 耗时：" + (System.currentTimeMillis() - start) + "ms");
            return result;
        } catch (Throwable throwable) {
            LOGGER.error("[ERR] " + method + " 异常：" + throwable.getMessage());
            throw throwable;
        }
    }

    static String sanitize(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(describe(args[i]));
        }
        builder.append(']');
        return builder.toString();
    }

    private static String describe(Object arg) {
        if (arg == null) {
            return "null";
        }
        if (arg instanceof LoginDTO) {
            LoginDTO dto = (LoginDTO) arg;
            return "LoginDTO{username='" + dto.getUsername() + "', password='***'}";
        }
        if (arg instanceof BindingResult || arg instanceof HttpSession || arg instanceof HttpServletRequest) {
            return arg.getClass().getSimpleName();
        }
        return String.valueOf(arg);
    }
}
