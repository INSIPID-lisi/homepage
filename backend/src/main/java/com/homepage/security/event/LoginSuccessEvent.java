package com.homepage.security.event;

public class LoginSuccessEvent extends SecurityEvent {

    public LoginSuccessEvent(Object source, String email, String ipAddress) {
        super(source, email, ipAddress);
    }
}
