package com.lab.emp.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordUtilTest {

    private static final String SALT = "a1b2c3d4e5f60718";
    private static final String ADMIN_HASH = "85cc99c527a2588b8d387b5a7f75008ac9561738dc9e18d1b9e74699e1718bbf";

    @Test
    public void seededAdminPasswordMatchesInitSql() {
        assertEquals(ADMIN_HASH, PasswordUtil.hash(SALT, "123456"));
        assertTrue(PasswordUtil.matches(SALT, "123456", ADMIN_HASH));
    }

    @Test
    public void wrongPasswordDoesNotMatch() {
        assertFalse(PasswordUtil.matches(SALT, "654321", ADMIN_HASH));
    }

    @Test
    public void hashIsNotThePlainPassword() {
        String hashed = PasswordUtil.hash(PasswordUtil.randomSalt(), "123456");
        assertFalse(hashed.contains("123456"));
        assertEquals(64, hashed.length());
    }
}
