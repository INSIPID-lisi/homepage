package com.homepage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    private Long id;
    private String email;
    private String password;
    private String role;
    private LocalDateTime createdTime;
}
