package com.codingplatform.fluxguard.service;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;

import com.codingplatform.fluxguard.model.FluxGuardRequestLog;

public interface FluxGuardLoggingService {

    void recordAuthenticatedRequest(HttpServletRequest request, HttpServletResponse response, long elapsedMilliseconds,
                                   Authentication authentication);

    List<FluxGuardRequestLog> getRecentRequests(Long userId, int limit);

    List<FluxGuardRequestLog> getLoginHistory(Long userId);
}
