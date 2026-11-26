package com.lab.emp.aspect;

import com.lab.emp.dto.LoginDTO;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WebLogAspectTest {

    @Test
    public void passwordIsMasked() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");
        String text = WebLogAspect.sanitize(new Object[]{dto});
        assertTrue(text.contains("admin"));
        assertTrue(text.contains("password='***'"));
        assertFalse(text.contains("123456"));
    }
}
