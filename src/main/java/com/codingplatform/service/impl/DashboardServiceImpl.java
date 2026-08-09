package com.codingplatform.service.impl;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.codingplatform.dto.DashboardView;
import com.codingplatform.service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    public DashboardView buildDashboardView(Authentication authentication) {
        String userName = (authentication != null && authentication.getName() != null)
                ? authentication.getName()
                : "Guest";

        DashboardView view = new DashboardView();
        view.setUserName(userName);
        view.setGreeting("Welcome back, " + userName + "!");
        view.setHeroSubtitle("Track your coding progress and contests.");
        view.setProblemsSolved(0);
        view.setContestsJoined(0);
        view.setSubmissions(0);
        view.setCurrentRank("Unranked");
        view.setActiveContestName("No active contest");
        view.setActiveContestStartTime("");
        view.setActiveContestDuration("");
        view.setRecentActivity(List.of("Welcome to FluxGuard!", "Complete your first challenge."));
        view.setUpcomingContests(List.of("No upcoming contests"));
        return view;
    }
}
