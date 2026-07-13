package com.homepage.security.event;

import lombok.Getter;

@Getter
public class RateLimitViolationEvent extends SecurityEvent {

    private final String endpoint;
    private final String rateLimitKey;

    public RateLimitViolationEvent(Object source, String email, String ipAddress, String endpoint, String rateLimitKey) {
        super(source, email, ipAddress);
        this.endpoint = endpoint;
        this.rateLimitKey = rateLimitKey;
    }

}
