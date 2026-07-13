package com.homepage.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    String key() default "";

    long windowSeconds() default 60;

    int maxRequests() default 10;

    RateLimitType type() default RateLimitType.IP_BASED;
}
