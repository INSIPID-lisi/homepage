package com.homepage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("social_link")
public class SocialLink {
    private Long id;
    private String platform;
    private String url;
    private String icon;
}
