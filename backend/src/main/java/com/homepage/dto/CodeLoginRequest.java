package com.homepage.dto;

import lombok.Data;

@Data
public class CodeLoginRequest {
    private String email;
    private String code;
}
