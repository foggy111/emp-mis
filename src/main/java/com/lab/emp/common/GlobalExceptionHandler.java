package com.lab.emp.common;

import org.apache.log4j.Logger;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理。必须放在 Spring MVC 子容器中，根容器里的 @RestControllerAdvice 不会处理请求。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBiz(BusinessException exception) {
        LOGGER.warn(exception.getMessage());
        return Result.fail(exception.getMessage());
    }

    /**
     * 有的接口只写了 @Valid、没有紧跟 BindingResult。这里兜住，避免落到框架错误页。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleInvalid(MethodArgumentNotValidException exception) {
        String message = "参数校验失败";
        if (exception.getBindingResult().getFieldError() != null
                && exception.getBindingResult().getFieldError().getDefaultMessage() != null) {
            message = exception.getBindingResult().getFieldError().getDefaultMessage();
        }
        return Result.fail(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleUnreadable(HttpMessageNotReadableException exception) {
        LOGGER.warn("请求体无法解析", exception);
        return Result.fail("请求体不是合法的 JSON");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return Result.fail("参数类型错误");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception exception) {
        LOGGER.error("未处理异常", exception);
        return Result.fail("系统繁忙，请稍后重试");
    }
}
