package com.codingplatform.fluxguard.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private final String instanceId;

    public FluxGuardLoggingServiceImpl(FluxGuardRequestLogRepository requestLogRepository,
                                       @Value("${fluxguard.instance-id:local}") String instanceId) {
        this.requestLogRepository = requestLogRepository;
        this.instanceId = instanceId;
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
        log.setResponseStatus(response != null ? response.getStatus() : 200);
        log.setResponseTimeMs(elapsedMilliseconds);
        log.setEventType("REQUEST");
        log.setInstanceId(instanceId);
        if (log.getResponseStatus() >= 400) {
            log.setFailureReason("HTTP request failed (status " + log.getResponseStatus() + ")");
        }
        log.setUserAgent(userAgent);
        log.setBrowser(browser);
        log.setOperatingSystem(operatingSystem);

        requestLogRepository.save(log);
        logger.debug("FluxGuard request logged for user={} endpoint={} status={} ms={}",
                user.getEmail(), requestUri, log.getResponseStatus(), elapsedMilliseconds);
    }

    @Override
    @Transactional
    public void recordFailedRequest(HttpServletRequest request, HttpServletResponse response, String reason,
                                   Authentication authentication) {
        FluxGuardRequestLog log = createRequestLog(request, response, authentication);
        log.setEventType("REQUEST");
        log.setFailureReason(reason);
        if (response != null) {
            log.setResponseStatus(response.getStatus());
        }
        requestLogRepository.save(log);
    }

    @Override
    @Transactional
    public void recordAuthenticationEvent(HttpServletRequest request, int status, String reason,
                                          Authentication authentication) {
        FluxGuardRequestLog log = createRequestLog(request, null, authentication);
        log.setEndpoint("/login");
        log.setHttpMethod("POST");
        log.setResponseStatus(status);
        log.setEventType("AUTHENTICATION");
        log.setFailureReason(reason);
        requestLogRepository.save(log);
    }

    private FluxGuardRequestLog createRequestLog(HttpServletRequest request, HttpServletResponse response,
                                                 Authentication authentication) {
        FluxGuardRequestLog log = new FluxGuardRequestLog();
        log.setTimestamp(LocalDateTime.now());
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            UserAccount user = details.getUserAccount();
            log.setUserId(user.getId());
            log.setUsername(user.getEmail());
        } else if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
            log.setUsername(authentication.getName());
        }
        log.setSessionId(request.getSession(false) != null ? request.getSession(false).getId() : "none");
        log.setIpAddress(resolveClientIp(request));
        log.setHttpMethod(request.getMethod());
        log.setEndpoint(request.getRequestURI());
        log.setResponseStatus(response != null ? response.getStatus() : 200);
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setBrowser(UserAgentUtils.detectBrowser(log.getUserAgent()));
        log.setOperatingSystem(UserAgentUtils.detectOperatingSystem(log.getUserAgent()));
        log.setEventType("REQUEST");
        log.setInstanceId(instanceId);
        return log;
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
                .filter(log -> "AUTHENTICATION".equals(log.getEventType()))
                .collect(java.util.stream.Collectors.toList());
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
