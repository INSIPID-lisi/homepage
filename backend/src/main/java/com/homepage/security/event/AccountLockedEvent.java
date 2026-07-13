package com.homepage.security.event;

import lombok.Getter;

@Getter
public class AccountLockedEvent extends SecurityEvent {

    private final long lockDurationSeconds;

    public AccountLockedEvent(Object source, String email, String ipAddress, long lockDurationSeconds) {
        super(source, email, ipAddress);
        this.lockDurationSeconds = lockDurationSeconds;
    }

}
