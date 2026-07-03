package com.homepage.service;

import com.homepage.dto.*;

public interface AuthService {
    void sendCode(String email);
    AuthResponse register(RegisterRequest req);
    AuthResponse login(LoginRequest req);
    AuthResponse loginByCode(CodeLoginRequest req);
    void changePassword(PasswordChangeRequest req);
}
