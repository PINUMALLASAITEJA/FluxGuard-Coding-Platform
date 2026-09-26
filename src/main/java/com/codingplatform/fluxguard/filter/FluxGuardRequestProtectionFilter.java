package com.codingplatform.fluxguard.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.time.Duration;
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
    private static final Pattern AUTH_BYPASS = Pattern.compile(
            "['\\\"]\\s*(?:or|and)\\s+['\\\"]?[^\\s'\\\"]+['\\\"]?\\s*=\\s*['\\\"]?[^\\s'\\\"]+['\\\"]?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern UNION_SELECT = Pattern.compile("\\bunion\\s+(?:all\\s+)?select\\b", Pattern.CASE_INSENSITIVE);

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
        return username != null && (AUTH_BYPASS.matcher(username).find() || UNION_SELECT.matcher(username).find());
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