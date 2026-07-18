package com.codingplatform.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.codingplatform.dto.DashboardView;
import com.codingplatform.service.DashboardService;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    // Constructor injection keeps the controller lightweight and testable.
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // Handle the dashboard page request and delegate view creation to the service layer.
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        DashboardView dashboardView = dashboardService.buildDashboardView(authentication);
        model.addAttribute("dashboardView", dashboardView);
        return "dashboard.html";
    }
}