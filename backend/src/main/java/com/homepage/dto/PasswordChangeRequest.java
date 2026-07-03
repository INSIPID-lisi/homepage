package com.homepage.dto;

import lombok.Data;

@Data
public class PasswordChangeRequest {
    private String oldPassword;
    private String email;
    private String code;
    private String newPassword;
    private String confirmPassword;
}
