package com.lab.emp.controller;

import com.lab.emp.common.Result;
import com.lab.emp.dto.LoginDTO;
import com.lab.emp.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * 登录与注册。这两个路径在拦截器中排除。
 */
@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDTO dto, HttpSession session) {
        if (dto == null || StringUtils.isBlank(dto.getUsername()) || StringUtils.isBlank(dto.getPassword())) {
            return Result.fail("用户名和密码不能为空");
        }
        String username = userService.login(dto.getUsername().trim(), dto.getPassword());
        if (username == null) {
            return Result.fail("用户名或密码错误");
        }
        session.setAttribute("loginUser", username);
        return Result.ok(username);
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody LoginDTO dto) {
        if (dto == null || StringUtils.isBlank(dto.getUsername()) || StringUtils.isBlank(dto.getPassword())) {
            return Result.fail("用户名和密码不能为空");
        }
        userService.register(dto.getUsername().trim(), dto.getPassword());
        return Result.ok(null);
    }
}
