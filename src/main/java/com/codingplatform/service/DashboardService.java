package com.codingplatform.service;

import org.springframework.security.core.Authentication;

import com.codingplatform.dto.DashboardView;

/**
 * Service contract for building the dashboard view for an authenticated user.
 */
public interface DashboardService {

    /**
     * Builds the dashboard view based on the current authentication context.
     *
     * @param authentication the current Spring Security authentication object
     * @return the dashboard view data transfer object for rendering the dashboard page
     */
    DashboardView buildDashboardView(Authentication authentication);
}
