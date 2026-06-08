package com.driverkonnect.backend.util;

import com.driverkonnect.backend.auth.JwtAuthenticationToken;
import com.driverkonnect.backend.dto.request.auth.AuthenticatedUserDto;
import com.driverkonnect.backend.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class AuthUtil {

    public static AuthenticatedUserDto getAuthenticatedUser() {
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException("You must be logged in to access this resource", 403);
        }

        return new AuthenticatedUserDto(
                authentication.getName(),
                authentication.getRole(),
                extractClientIp()
        );
    }

    public static String getUsernameOrAnonymous() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }
        return auth.getName();
    }

    private static String extractClientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isEmpty()) ip = ip.split(",")[0].trim();
            if (isInvalid(ip)) ip = request.getHeader("X-Real-IP");
            if (isInvalid(ip)) ip = request.getRemoteAddr();
            return ip;
        } catch (IllegalStateException e) {
            log.warn("Request context not available, cannot extract client IP");
            return "unknown";
        }
    }

    private static boolean isInvalid(String ip) {
        return ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip);
    }
}
