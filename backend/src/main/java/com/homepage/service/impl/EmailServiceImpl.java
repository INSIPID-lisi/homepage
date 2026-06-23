package com.homepage.service.impl;

import com.homepage.exception.BusinessException;
import com.homepage.exception.ErrorCode;
import com.homepage.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    @Override
    public void sendVerificationCode(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        codeStore.put(email, new CodeEntry(code, LocalDateTime.now().plusMinutes(5)));

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("Homepage 验证码");
            helper.setText("您的验证码是：<b>" + code + "</b>，5分钟内有效。", true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            codeStore.remove(email);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to send email: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        CodeEntry entry = codeStore.get(email);
        if (entry == null) return false;
        if (entry.expiry.isBefore(LocalDateTime.now())) {
            codeStore.remove(email);
            return false;
        }
        if (!entry.code.equals(code)) return false;
        codeStore.remove(email);
        return true;
    }

    private record CodeEntry(String code, LocalDateTime expiry) {}
}
