package com.homepage.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private Login login = new Login();

    @Data
    public static class Login {
        private int maxAttempts = 5;
        private long attemptWindowSeconds = 900;
        private long lockDurationSeconds = 1800;
    }
}
