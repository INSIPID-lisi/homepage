package com.homepage.security.listener;

import com.homepage.security.event.AccountLockedEvent;
import com.homepage.security.event.LoginFailureEvent;
import com.homepage.security.event.LoginSuccessEvent;
import com.homepage.security.event.RateLimitViolationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventListener {

    private static final Logger auditLog = LoggerFactory.getLogger("SECURITY_AUDIT");

    @EventListener
    public void onLoginSuccess(LoginSuccessEvent event) {
        auditLog.info("LOGIN_SUCCESS email={} ip={}", event.getEmail(), event.getIpAddress());
    }

    @EventListener
    public void onLoginFailure(LoginFailureEvent event) {
        auditLog.warn("LOGIN_FAILURE email={} ip={} reason={} remaining={}",
                event.getEmail(), event.getIpAddress(), event.getReason(), event.getRemainingAttempts());
    }

    @EventListener
    public void onAccountLocked(AccountLockedEvent event) {
        auditLog.warn("ACCOUNT_LOCKED email={} ip={} duration={}s",
                event.getEmail(), event.getIpAddress(), event.getLockDurationSeconds());
    }

    @EventListener
    public void onRateLimitViolation(RateLimitViolationEvent event) {
        auditLog.warn("RATE_LIMIT email={} ip={} endpoint={} key={}",
                event.getEmail(), event.getIpAddress(), event.getEndpoint(), event.getRateLimitKey());
    }
}
