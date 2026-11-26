package com.lab.emp.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ResultAndExceptionTest {

    @Test
    public void resultCodes() {
        assertEquals(Integer.valueOf(200), Result.ok("ok").getCode());
        assertEquals("success", Result.ok("ok").getMessage());
        assertEquals(Integer.valueOf(500), Result.fail("失败").getCode());
        assertEquals("失败", Result.fail("失败").getMessage());
        assertNull(Result.fail("失败").getData());
        assertEquals(Integer.valueOf(401), Result.unauthorized("未登录").getCode());
    }

    @Test
    public void businessExceptionKeepsMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Result<Void> result = handler.handleBiz(new BusinessException("用户名已存在"));
        assertEquals(Integer.valueOf(500), result.getCode());
        assertEquals("用户名已存在", result.getMessage());
    }

    @Test
    public void fallbackDoesNotLeakStack() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Result<Void> result = handler.handleOther(new IllegalStateException("sql detail /etc/passwd"));
        assertEquals(Integer.valueOf(500), result.getCode());
        assertEquals("系统繁忙，请稍后重试", result.getMessage());
    }
}
