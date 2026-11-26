package com.lab.emp.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 口令摘要。算法为 SHA-256(UTF-8(salt + password))，输出小写十六进制。
 * sql/init.sql 中 admin 账号使用同一算法，盐为 a1b2c3d4e5f60718。
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String randomSalt() {
        byte[] buffer = new byte[8];
        RANDOM.nextBytes(buffer);
        return toHex(buffer);
    }

    public static String hash(String salt, String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest((salt + rawPassword).getBytes(StandardCharsets.UTF_8));
            return toHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", exception);
        }
    }

    public static boolean matches(String salt, String rawPassword, String expectedHex) {
        if (salt == null || rawPassword == null || expectedHex == null) {
            return false;
        }
        byte[] actual = hash(salt, rawPassword).getBytes(StandardCharsets.UTF_8);
        byte[] expected = expectedHex.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(actual, expected);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(Character.forDigit((value >> 4) & 0xF, 16));
            builder.append(Character.forDigit(value & 0xF, 16));
        }
        return builder.toString();
    }
}
