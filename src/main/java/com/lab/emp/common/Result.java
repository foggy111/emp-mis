package com.lab.emp.common;

import java.io.Serializable;

/**
 * 统一返回。code：200 成功 / 500 失败 / 401 未登录。
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<T>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<T>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> unauthorized(String message) {
        Result<T> result = new Result<T>();
        result.setCode(401);
        result.setMessage(message);
        return result;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
