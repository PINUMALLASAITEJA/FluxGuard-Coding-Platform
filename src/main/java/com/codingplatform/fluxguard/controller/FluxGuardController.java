package com.codingplatform.fluxguard.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.codingplatform.fluxguard.dto.FluxGuardDashboardView;
import com.codingplatform.fluxguard.model.FluxGuardRequestLog;
import com.codingplatform.fluxguard.repository.FluxGuardRequestLogRepository;
import com.codingplatform.fluxguard.service.FluxGuardLoggingService;
import com.codingplatform.model.UserAccount;
import com.codingplatform.security.CustomUserDetails;

@Controller
public class FluxGuardController {

    private final FluxGuardRequestLogRepository requestLogRepository;
    private final FluxGuardLoggingService fluxGuardLoggingService;

    public FluxGuardController(FluxGuardRequestLogRepository requestLogRepository,
                              FluxGuardLoggingService fluxGuardLoggingService) {
        this.requestLogRepository = requestLogRepository;
        this.fluxGuardLoggingService = fluxGuardLoggingService;
    }

    @GetMapping("/fluxguard")
    public String fluxguard(Authentication authentication, Model model) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            return "redirect:/login";
        }

        UserAccount user = details.getUserAccount();
        List<FluxGuardRequestLog> recentLogs = requestLogRepository.findTop20ByUserIdOrderByTimestampDesc(user.getId());
        List<FluxGuardRequestLog> allLogs = requestLogRepository.findByUserIdOrderByTimestampDesc(user.getId());

        FluxGuardDashboardView view = buildDashboardView(user, recentLogs, allLogs, authentication);
        model.addAttribute("dashboard", view);
        return "fluxguard";
    }

    private FluxGuardDashboardView buildDashboardView(UserAccount user, List<FluxGuardRequestLog> recentLogs,
                                                     List<FluxGuardRequestLog> allLogs, Authentication authentication) {
        FluxGuardDashboardView view = new FluxGuardDashboardView();
        view.setUsername(user.getFullName());

        List<FluxGuardRequestLog> usableLogs = allLogs == null ? List.of() : allLogs;
        FluxGuardRequestLog latestLog = usableLogs.isEmpty() ? null : usableLogs.get(0);

        if (latestLog != null) {
            view.setCurrentIp(latestLog.getIpAddress());
            view.setBrowser(latestLog.getBrowser());
            view.setOperatingSystem(latestLog.getOperatingSystem());
            view.setLoginTime(latestLog.getTimestamp().toString());
        }

        long totalRequests = usableLogs.size();
        long successfulRequests = usableLogs.stream().filter(log -> log.getResponseStatus() != null && log.getResponseStatus() >= 200 && log.getResponseStatus() < 400).count();
        long failedRequests = usableLogs.stream().filter(log -> log.getResponseStatus() != null && log.getResponseStatus() >= 400).count();
        double averageResponseTime = usableLogs.stream()
                .filter(log -> log.getResponseTimeMs() != null)
                .mapToLong(FluxGuardRequestLog::getResponseTimeMs)
                .average()
                .orElse(0d);

        Map<String, Long> endpointCounts = usableLogs.stream()
                .collect(Collectors.groupingBy(FluxGuardRequestLog::getEndpoint, LinkedHashMap::new, Collectors.counting()));
        String mostVisitedEndpoint = endpointCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        LocalDateTime today = LocalDateTime.now();
        long todayRequests = usableLogs.stream()
                .filter(log -> log.getTimestamp() != null && log.getTimestamp().toLocalDate().equals(today.toLocalDate()))
                .count();

        int score = 92;
        score -= Math.min(15, failedRequests > 0 ? (int) failedRequests : 0);
        score -= Math.min(10, Math.max(0, totalRequests > 100 ? 10 : 0));
        if (score < 0) score = 0;
        view.setSecurityScore(score);
        view.setTotalRequests(totalRequests);
        view.setTodayRequests(todayRequests);
        view.setSuccessfulRequests(successfulRequests);
        view.setFailedRequests(failedRequests);
        view.setAverageResponseTime(Math.round(averageResponseTime * 10.0) / 10.0);
        view.setMostVisitedEndpoint(mostVisitedEndpoint);
        view.setRecentActivity(recentLogs == null ? new ArrayList<>() : recentLogs);

        List<String> events = new ArrayList<>();
        if (failedRequests > 0) {
            events.add("Detected " + failedRequests + " failed request(s) in recent activity.");
        }
        if (recentLogs != null && recentLogs.stream().anyMatch(log -> log.getResponseStatus() != null && log.getResponseStatus() >= 400)) {
            events.add("Access denied events were observed.");
        }
        if (events.isEmpty()) {
            events.add("No active security alerts.");
        }
        view.setSecurityEvents(events);

        List<String> deviceSummaries = new ArrayList<>();
        usableLogs.stream()
                .collect(Collectors.groupingBy(log -> log.getBrowser() + " | " + log.getOperatingSystem() + " | " + log.getIpAddress(), LinkedHashMap::new, Collectors.counting()))
                .forEach((device, count) -> deviceSummaries.add(device + " (" + count + " requests)"));
        view.setDevices(deviceSummaries.stream().limit(5).collect(Collectors.toList()));
        view.setLoginHistory(usableLogs.stream().limit(10).collect(Collectors.toList()));

        return view;
    }
}
