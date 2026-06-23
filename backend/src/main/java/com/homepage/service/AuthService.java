package com.homepage.service;

import com.homepage.dto.AuthResponse;
import com.homepage.dto.CodeLoginRequest;
import com.homepage.dto.LoginRequest;
import com.homepage.dto.RegisterRequest;

public interface AuthService {
    void sendCode(String email);
    AuthResponse register(RegisterRequest req);
    AuthResponse login(LoginRequest req);
    AuthResponse loginByCode(CodeLoginRequest req);
}
