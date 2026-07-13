package com.homepage.security.aspect;

import com.homepage.exception.BusinessException;
import com.homepage.exception.ErrorCode;
import com.homepage.security.annotation.RateLimit;
import com.homepage.security.annotation.RateLimitType;
import com.homepage.security.event.RateLimitViolationEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(rateLimit)")
    public Object check(ProceedingJoinPoint jp, RateLimit rateLimit) throws Throwable {
        String ip = getClientIp();
        String paramKey = resolveKey(rateLimit.key(), jp);
        String redisKey = buildRedisKey(rateLimit.type(), ip, paramKey);

        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, rateLimit.windowSeconds(), TimeUnit.SECONDS);
        }

        if (count != null && count > rateLimit.maxRequests()) {
            eventPublisher.publishEvent(new RateLimitViolationEvent(this, paramKey, ip,
                    jp.getSignature().toShortString(), redisKey));
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "too many requests, please try later");
        }

        return jp.proceed();
    }

    private String buildRedisKey(RateLimitType type, String ip, String paramKey) {
        return switch (type) {
            case IP_BASED -> "ratelimit:ip:" + ip;
            case PARAM_BASED -> "ratelimit:param:" + paramKey;
            case COMBINED -> "ratelimit:combined:" + ip + ":" + paramKey;
        };
    }

    private String resolveKey(String expression, ProceedingJoinPoint jp) {
        if (expression.isEmpty()) {
            return "";
        }
        MethodSignature signature = (MethodSignature) jp.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = jp.getArgs();
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                ctx.setVariable(paramNames[i], args[i]);
            }
        }
        try {
            return parser.parseExpression(expression).getValue(ctx, String.class);
        } catch (Exception e) {
            return expression;
        }
    }

    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        HttpServletRequest request = attrs.getRequest();
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
