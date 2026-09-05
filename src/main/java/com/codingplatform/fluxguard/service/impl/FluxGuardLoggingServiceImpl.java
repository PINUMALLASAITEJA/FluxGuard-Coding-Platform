package com.codingplatform.fluxguard.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codingplatform.fluxguard.model.FluxGuardRequestLog;
import com.codingplatform.fluxguard.repository.FluxGuardRequestLogRepository;
import com.codingplatform.fluxguard.service.FluxGuardLoggingService;
import com.codingplatform.fluxguard.util.UserAgentUtils;
import com.codingplatform.model.UserAccount;
import com.codingplatform.security.CustomUserDetails;

@Service
public class FluxGuardLoggingServiceImpl implements FluxGuardLoggingService {

    private static final Logger logger = LoggerFactory.getLogger(FluxGuardLoggingServiceImpl.class);

    private final FluxGuardRequestLogRepository requestLogRepository;

    public FluxGuardLoggingServiceImpl(FluxGuardRequestLogRepository requestLogRepository) {
        this.requestLogRepository = requestLogRepository;
    }

    @Override
    @Transactional
    public void recordAuthenticatedRequest(HttpServletRequest request, HttpServletResponse response,
                                          long elapsedMilliseconds, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            return;
        }

        UserAccount user = details.getUserAccount();
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String userAgent = request.getHeader("User-Agent");
        String browser = UserAgentUtils.detectBrowser(userAgent);
        String operatingSystem = UserAgentUtils.detectOperatingSystem(userAgent);

        FluxGuardRequestLog log = new FluxGuardRequestLog();
        log.setTimestamp(LocalDateTime.now());
        log.setUserId(user.getId());
        log.setUsername(user.getEmail());
        log.setSessionId(request.getSession(false) != null ? request.getSession(false).getId() : "none");
        log.setIpAddress(resolveClientIp(request));
        log.setHttpMethod(request.getMethod());
        log.setEndpoint(requestUri);
        log.setQueryParameters(queryString);
        log.setResponseStatus(response != null ? response.getStatus() : 200);
        log.setResponseTimeMs(elapsedMilliseconds);
        log.setUserAgent(userAgent);
        log.setBrowser(browser);
        log.setOperatingSystem(operatingSystem);

        requestLogRepository.save(log);
        logger.debug("FluxGuard request logged for user={} endpoint={} status={} ms={}",
                user.getEmail(), requestUri, log.getResponseStatus(), elapsedMilliseconds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FluxGuardRequestLog> getRecentRequests(Long userId, int limit) {
        List<FluxGuardRequestLog> logs = requestLogRepository.findByUserIdOrderByTimestampDesc(userId);
        if (logs.size() > limit) {
            return logs.subList(0, limit);
        }
        return logs;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FluxGuardRequestLog> getLoginHistory(Long userId) {
        return requestLogRepository.findByUserIdOrderByTimestampDesc(userId).stream()
                .filter(log -> "/login".equals(log.getEndpoint()) || "/logout".equals(log.getEndpoint()))
                .collect(Collectors.toList());
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }
}
