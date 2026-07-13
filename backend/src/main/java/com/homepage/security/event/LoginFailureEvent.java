package com.homepage.security.event;

import lombok.Getter;

@Getter
public class LoginFailureEvent extends SecurityEvent {

    private final String reason;
    private final int remainingAttempts;

    public LoginFailureEvent(Object source, String email, String ipAddress, String reason, int remainingAttempts) {
        super(source, email, ipAddress);
        this.reason = reason;
        this.remainingAttempts = remainingAttempts;
    }

}
