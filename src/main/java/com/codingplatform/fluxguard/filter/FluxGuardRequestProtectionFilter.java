package com.codingplatform.fluxguard.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.codingplatform.fluxguard.service.FluxGuardLoggingService;
import com.codingplatform.fluxguard.service.RequestDeduplicationStore;

public class FluxGuardRequestProtectionFilter extends OncePerRequestFilter {

    private static final Duration DUPLICATE_WINDOW = Duration.ofSeconds(3);
                private static final Pattern SQLI_AUTH_BYPASS = Pattern.compile(
                    "(?i)(?:\\b(?:or|and)\\b\\s+(?:\\d+|[a-z0-9_.-]+|['\\\"][^'\\\"]+['\\\"])\\s*(?:=|!=|<>|<|>|like)\\s*(?:\\d+|[a-z0-9_.-]+|['\\\"][^'\\\"]+['\\\"])|"
                        + "\\bunion\\b\\s+(?:all\\s+)?\\bselect\\b|"
                        + "(?:--|/\\*|;\\s*(?:drop|delete|update|insert|alter)\\b))");

    private final FluxGuardLoggingService loggingService;
    private final RequestDeduplicationStore deduplicationStore;

    public FluxGuardRequestProtectionFilter(FluxGuardLoggingService loggingService,
                                           RequestDeduplicationStore deduplicationStore) {
        this.loggingService = loggingService;
        this.deduplicationStore = deduplicationStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (isLoginPost(request) && isSuspiciousLogin(request.getParameter("username"))) {
            Authentication authentication = currentAuthentication();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain;charset=UTF-8");
            loggingService.recordAuthenticationEvent(request, HttpServletResponse.SC_FORBIDDEN,
                    "Suspicious request detected", authentication);
            response.getWriter().write("Action Denied!");
            return;
        }

        if (isProtectedAction(request) && isDuplicate(request)) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.setContentType("text/plain;charset=UTF-8");
            loggingService.recordFailedRequest(request, response, "Duplicate request blocked", currentAuthentication());
            response.getWriter().write("Duplicate request blocked");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isLoginPost(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(request.getRequestURI());
    }

    private boolean isSuspiciousLogin(String username) {
        if (username == null) {
            return false;
        }

        String normalized = username.replaceAll("\\s+", " ").trim();
        String lower = normalized.toLowerCase(Locale.ROOT);

        if (lower.contains("union select") || lower.contains("union all select")) {
            return true;
        }

        if (lower.contains("or 1=1") || lower.contains("and 1=1")
            || lower.contains("or '1'='1") || lower.contains("and '1'='1")) {
            return true;
        }

        if ((lower.contains("' or ") || lower.contains("\" or ") || lower.contains("' and ") || lower.contains("\" and "))
                && (lower.contains("=") || lower.contains(" like "))) {
            return true;
        }

        if (lower.contains("--") || lower.contains("/*") || lower.contains(";drop ")
                || lower.contains(";delete ") || lower.contains(";update ")
                || lower.contains(";insert ") || lower.contains(";alter ")) {
            return true;
        }

        return SQLI_AUTH_BYPASS.matcher(normalized).find();
    }

    private boolean isProtectedAction(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return "/submit".equals(path)
                || "/problems".equals(path)
                || "/problems/import".equals(path)
                || "/problems/import-exercism".equals(path)
                || path.matches("/problems/\\d+/solve")
                || path.matches("/problems/update/\\d+")
                || path.matches("/problems/delete/\\d+");
    }

    private boolean isDuplicate(HttpServletRequest request) {
        String sessionId = request.getSession(false) == null
                ? request.getRemoteAddr() : request.getSession(false).getId();
        String key = sessionId + ':' + request.getRequestURI() + ':' + requestFingerprint(request);
        return !deduplicationStore.claim(key, DUPLICATE_WINDOW);
    }

    private String requestFingerprint(HttpServletRequest request) {
        StringBuilder canonical = new StringBuilder();
        request.getParameterMap().entrySet().stream()
                .filter(entry -> !entry.getKey().toLowerCase().contains("password")
                        && !entry.getKey().toLowerCase().contains("token"))
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    canonical.append(entry.getKey()).append('=');
                    Arrays.stream(entry.getValue()).sorted().forEach(value -> canonical.append(value).append('\u0000'));
                });
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}