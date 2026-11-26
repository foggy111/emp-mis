package com.lab.emp.interceptor;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoginInterceptorTest {

    private final LoginInterceptor interceptor = new LoginInterceptor();

    @Test
    public void missingSessionReturns401Json() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/emps");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean passed = interceptor.preHandle(request, response, new Object());
        assertFalse(passed);
        assertEquals(401, response.getStatus());
        assertEquals(LoginInterceptor.UNAUTHORIZED_JSON, response.getContentAsString());
    }

    @Test
    public void loginUserPasses() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/emps");
        request.getSession().setAttribute("loginUser", "admin");
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    public void optionsPreflightPasses() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/emps");
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }
}
