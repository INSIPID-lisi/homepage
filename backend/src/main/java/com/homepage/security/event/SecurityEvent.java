package com.homepage.security.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public abstract class SecurityEvent extends java.util.EventObject {

    private final String email;
    private final String ipAddress;
    private final LocalDateTime timestamp;

    public SecurityEvent(Object source, String email, String ipAddress) {
        super(source);
        this.email = email;
        this.ipAddress = ipAddress;
        this.timestamp = LocalDateTime.now();
    }

}
