package com.homepage.service.impl;

import com.homepage.dto.AuthResponse;
import com.homepage.dto.CodeLoginRequest;
import com.homepage.dto.LoginRequest;
import com.homepage.dto.RegisterRequest;
import com.homepage.entity.User;
import com.homepage.exception.BusinessException;
import com.homepage.exception.ErrorCode;
import com.homepage.mapper.UserMapper;
import com.homepage.service.AuthService;
import com.homepage.service.EmailService;
import com.homepage.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void sendCode(String email) {
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "invalid email");
        }
        emailService.sendVerificationCode(email);
    }

    @Override
    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail();
        String password = req.getPassword();
        String code = req.getCode();

        if (email == null || password == null || code == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "email, password and code required");
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "invalid email");
        }
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "password too short, min 6 chars");
        }

        if (!emailService.verifyCode(email, code)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "invalid or expired code");
        }

        if (userMapper.selectByEmail(email) != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "email already registered");
        }

        boolean isFirst = userMapper.selectCount(null) == 0;

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(isFirst ? "ADMIN" : "USER");
        user.setCreatedTime(LocalDateTime.now());
        userMapper.insert(user);

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getRole());
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail();
        String password = req.getPassword();

        if (email == null || password == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "email and password required");
        }

        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "invalid email or password");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "invalid email or password");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getRole());
    }

    @Override
    public AuthResponse loginByCode(CodeLoginRequest req) {
        String email = req.getEmail();
        String code = req.getCode();

        if (email == null || code == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "email and code required");
        }

        if (!emailService.verifyCode(email, code)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "invalid or expired code");
        }

        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "user not found, please register first");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getRole());
    }
}
