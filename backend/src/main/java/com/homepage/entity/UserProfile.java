package com.homepage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_profile")
public class UserProfile {
    private Long id;
    private String name;
    private String avatar;
    private String bio;
}
