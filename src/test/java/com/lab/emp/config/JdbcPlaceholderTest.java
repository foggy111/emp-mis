package com.lab.emp.config;

import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

public class JdbcPlaceholderTest {

    @Test
    public void committedJdbcFileUsesPlaceholderPassword() {
        InputStream in = getClass().getClassLoader().getResourceAsStream("jdbc.properties");
        assertTrue(in != null);
        String text;
        Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name());
        try {
            scanner.useDelimiter("\\A");
            text = scanner.hasNext() ? scanner.next() : "";
        } finally {
            scanner.close();
        }
        assertTrue(text.contains("jdbc.password=your_password"));
        assertTrue(!text.contains("jdbc.password=root"));
    }
}
