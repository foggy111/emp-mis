package com.lab.emp.common;

/**
 * 可预期的业务失败。消息会原样返回给调用方。
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }
}
