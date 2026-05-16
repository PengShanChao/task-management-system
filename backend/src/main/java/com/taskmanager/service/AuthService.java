package com.taskmanager.service;

import com.taskmanager.dto.LoginRequest;
import com.taskmanager.dto.LoginResponse;
import com.taskmanager.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse register(RegisterRequest request);
}
