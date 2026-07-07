package com.homepage.service.impl;

import com.homepage.exception.BusinessException;
import com.homepage.exception.ErrorCode;
import com.homepage.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String from;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final long CAPTCHA_TTL_MINUTES = 5;

    @Override
    public void sendVerificationCode(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        redisTemplate.opsForValue().set(CAPTCHA_KEY_PREFIX + email, code, CAPTCHA_TTL_MINUTES, TimeUnit.MINUTES);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("Homepage 验证码");
            helper.setText("您的验证码是：<b>" + code + "</b>，5分钟内有效，请不要泄露给他人呐。", true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            redisTemplate.delete(CAPTCHA_KEY_PREFIX + email);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to send email: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().getAndDelete(CAPTCHA_KEY_PREFIX + email);
        return code.equals(storedCode);
    }
}
