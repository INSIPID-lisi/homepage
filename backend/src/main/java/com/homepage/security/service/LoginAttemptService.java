package com.homepage.security.service;

import com.homepage.security.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String ATTEMPT_PREFIX = "login:attempt:";
    private static final String LOCK_PREFIX = "login:locked:";

    private final StringRedisTemplate redisTemplate;
    private final SecurityProperties securityProperties;

    public boolean isLocked(String email) {
        return redisTemplate.hasKey(LOCK_PREFIX + email);
    }

    public void recordFailedAttempt(String email) {
        String attemptKey = ATTEMPT_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(attemptKey);
        if (count != null && count == 1) {
            redisTemplate.expire(attemptKey, securityProperties.getLogin().getAttemptWindowSeconds(), TimeUnit.SECONDS);
        }
        if (count != null && count >= securityProperties.getLogin().getMaxAttempts()) {
            String lockKey = LOCK_PREFIX + email;
            redisTemplate.opsForValue().set(lockKey, "1", securityProperties.getLogin().getLockDurationSeconds(), TimeUnit.SECONDS);
            redisTemplate.delete(attemptKey);
        }
    }

    public void recordSuccessfulAttempt(String email) {
        redisTemplate.delete(ATTEMPT_PREFIX + email);
    }

    public int getRemainingAttempts(String email) {
        String countStr = redisTemplate.opsForValue().get(ATTEMPT_PREFIX + email);
        if (countStr == null) {
            return securityProperties.getLogin().getMaxAttempts();
        }
        int used = Integer.parseInt(countStr);
        return Math.max(0, securityProperties.getLogin().getMaxAttempts() - used);
    }
}
