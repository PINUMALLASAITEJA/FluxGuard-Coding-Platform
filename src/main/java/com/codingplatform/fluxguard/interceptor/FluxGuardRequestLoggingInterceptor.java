package com.codingplatform.fluxguard.interceptor;

import java.time.Duration;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.codingplatform.fluxguard.service.FluxGuardLoggingService;

@Component
public class FluxGuardRequestLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "fluxguard.startTime";

    private final FluxGuardLoggingService fluxGuardLoggingService;

    public FluxGuardRequestLoggingInterceptor(FluxGuardLoggingService fluxGuardLoggingService) {
        this.fluxGuardLoggingService = fluxGuardLoggingService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
            return;
        }

        Object startTimeAttr = request.getAttribute(START_TIME_ATTRIBUTE);
        long elapsedMilliseconds = startTimeAttr instanceof Long startTime
                ? Math.max(0L, System.currentTimeMillis() - startTime)
                : 0L;

        fluxGuardLoggingService.recordAuthenticatedRequest(request, response, elapsedMilliseconds, authentication);
    }
}
