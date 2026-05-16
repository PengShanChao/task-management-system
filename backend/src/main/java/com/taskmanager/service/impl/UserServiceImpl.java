package com.taskmanager.service.impl;

import com.taskmanager.entity.User;
import com.taskmanager.mapper.UserMapper;
import com.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public List<User> listUsers() {
        return userMapper.selectList(null);
    }
}
