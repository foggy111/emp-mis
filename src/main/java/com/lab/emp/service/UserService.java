package com.lab.emp.service;

/**
 * 会话登录。校验通过时返回用户名，失败返回 null。
 */
public interface UserService {

    String login(String username, String rawPassword);

    void register(String username, String rawPassword);
}
