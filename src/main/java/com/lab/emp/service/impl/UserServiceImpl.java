package com.lab.emp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.emp.common.BusinessException;
import com.lab.emp.common.PasswordUtil;
import com.lab.emp.entity.User;
import com.lab.emp.mapper.UserMapper;
import com.lab.emp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 注册时生成盐并保存摘要。登录时用同一算法比对。
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String login(String username, String rawPassword) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !PasswordUtil.matches(user.getSalt(), rawPassword, user.getPasswordHash())) {
            return null;
        }
        return user.getUsername();
    }

    @Override
    public void register(String username, String rawPassword) {
        if (rawPassword.length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (exists != null && exists.longValue() > 0L) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        String salt = PasswordUtil.randomSalt();
        user.setSalt(salt);
        user.setPasswordHash(PasswordUtil.hash(salt, rawPassword));
        userMapper.insert(user);
    }
}
