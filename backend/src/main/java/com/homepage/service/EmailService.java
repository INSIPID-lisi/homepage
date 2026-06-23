package com.homepage.service;

public interface EmailService {
    void sendVerificationCode(String email);
    boolean verifyCode(String email, String code);
}
