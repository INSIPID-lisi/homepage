package com.homepage.util;

import com.homepage.entity.User;
import com.homepage.exception.BusinessException;
import com.homepage.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof User user)) {
            return null;
        }
        return user;
    }

    public static void requireAdmin() {
        User user = getCurrentUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "not authenticated");
        }
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "admin only");
        }
    }
}
