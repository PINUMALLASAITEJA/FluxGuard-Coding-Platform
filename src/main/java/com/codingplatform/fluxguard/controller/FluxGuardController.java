package com.codingplatform.fluxguard.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.codingplatform.fluxguard.dto.FluxGuardDashboardView;
import com.codingplatform.fluxguard.model.FluxGuardRequestLog;
import com.codingplatform.fluxguard.repository.FluxGuardRequestLogRepository;
import com.codingplatform.security.CustomUserDetails;

@Controller
public class FluxGuardController {

    private final FluxGuardRequestLogRepository requestLogRepository;
    private final int heavyTrafficThreshold;

    public FluxGuardController(FluxGuardRequestLogRepository requestLogRepository,
                              @Value("${fluxguard.heavy-traffic-threshold:50}") int heavyTrafficThreshold) {
        this.requestLogRepository = requestLogRepository;
        this.heavyTrafficThreshold = heavyTrafficThreshold;
    }

    @GetMapping("/fluxguard")
    public String fluxguard(Authentication authentication, Model model) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfDay.plusDays(1);
        LocalDateTime activeSince = LocalDateTime.now().minus(5, ChronoUnit.MINUTES);
        long currentUsers = requestLogRepository.countActiveSessionsSince(activeSince);
        List<FluxGuardRequestLog> failures = requestLogRepository
            .findTop100ByTimestampGreaterThanEqualAndTimestampLessThanAndResponseStatusGreaterThanEqualOrderByTimestampDesc(
                startOfDay, startOfTomorrow, 400);
        List<FluxGuardDashboardView.InstanceTraffic> distribution = requestLogRepository
            .findInstanceUserCountsSince(activeSince).stream()
            .map(item -> new FluxGuardDashboardView.InstanceTraffic(item.getInstanceId(), item.getActiveUsers(),
                currentUsers == 0 ? 0 : (int) Math.round(item.getActiveUsers() * 100.0 / currentUsers)))
            .collect(Collectors.toList());

        FluxGuardDashboardView view = new FluxGuardDashboardView();
        view.setTodayRequests(requestLogRepository.countByTimestampGreaterThanEqualAndTimestampLessThan(startOfDay, startOfTomorrow));
        view.setSuccessfulRequests(requestLogRepository.countByTimestampGreaterThanEqualAndTimestampLessThanAndResponseStatusBetween(
            startOfDay, startOfTomorrow, 200, 399));
        view.setFailedRequests(requestLogRepository.countByTimestampGreaterThanEqualAndTimestampLessThanAndResponseStatusGreaterThanEqual(
            startOfDay, startOfTomorrow, 400));
        view.setCurrentUsers(currentUsers);
        view.setHeavyTrafficThreshold(heavyTrafficThreshold);
        view.setHeavyTraffic(currentUsers > heavyTrafficThreshold);
        view.setFailedRequestDetails(failures);
        view.setInstanceTraffic(distribution);
        view.setSecurityEvents(view.getFailedRequests() == 0 ? List.of("No active security alerts.")
            : List.of(view.getFailedRequests() + " failed request(s) recorded today."));
        model.addAttribute("dashboard", view);
        return "fluxguard";
    }
}
