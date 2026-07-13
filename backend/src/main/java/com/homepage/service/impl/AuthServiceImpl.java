package com.homepage.service.impl;

import com.homepage.dto.*;
import com.homepage.entity.User;
import com.homepage.exception.BusinessException;
import com.homepage.exception.ErrorCode;
import com.homepage.mapper.UserMapper;
import com.homepage.security.event.AccountLockedEvent;
import com.homepage.security.event.LoginFailureEvent;
import com.homepage.security.annotation.RateLimit;
import com.homepage.security.annotation.RateLimitType;
import com.homepage.security.event.LoginSuccessEvent;
import com.homepage.security.service.LoginAttemptService;
import com.homepage.service.AuthService;
import com.homepage.service.EmailService;
import com.homepage.util.JwtUtil;
import com.homepage.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @RateLimit(key = "#email", windowSeconds = 60, maxRequests = 1, type = RateLimitType.PARAM_BASED)
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

        String ip = getClientIp();

        if (loginAttemptService.isLocked(email)) {
            eventPublisher.publishEvent(new LoginFailureEvent(this, email, ip, "account locked", 0));
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED, "account locked due to too many failed attempts, please try later");
        }

        User user = userMapper.selectByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            loginAttemptService.recordFailedAttempt(email);
            int remaining = loginAttemptService.getRemainingAttempts(email);
            eventPublisher.publishEvent(new LoginFailureEvent(this, email, ip, "invalid credentials", remaining));

            if (loginAttemptService.isLocked(email)) {
                eventPublisher.publishEvent(new AccountLockedEvent(this, email, ip, 1800));
            }

            throw new BusinessException(ErrorCode.BAD_REQUEST, "invalid email or password");
        }

        loginAttemptService.recordSuccessfulAttempt(email);
        eventPublisher.publishEvent(new LoginSuccessEvent(this, email, ip));

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

    @Override
    public void changePassword(PasswordChangeRequest req) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }

        String newPassword = req.getNewPassword();
        String confirmPassword = req.getConfirmPassword();

        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "新密码至少6位");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "两次输入的密码不一致");
        }

        if (req.getOldPassword() != null) {
            // 模式一：旧密码验证
            if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "旧密码错误");
            }
        } else if (req.getEmail() != null && req.getCode() != null) {
            // 模式二：邮箱验证码验证
            if (!user.getEmail().equals(req.getEmail())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "邮箱与当前账号不匹配");
            }
            if (!emailService.verifyCode(req.getEmail(), req.getCode())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码错误或已过期");
            }
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请提供旧密码或邮箱验证码");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        HttpServletRequest request = attrs.getRequest();
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
