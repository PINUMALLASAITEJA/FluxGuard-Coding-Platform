package com.codingplatform.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.codingplatform.dto.DashboardView;
import com.codingplatform.model.UserAccount;
import com.codingplatform.repository.UserAccountRepository;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final UserAccountRepository userAccountRepository;

    public DashboardServiceImpl(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public DashboardView buildDashboardView(Authentication authentication) {
        String email = authentication.getName();
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        DashboardView dashboardView = new DashboardView();
        dashboardView.setUserName(userAccount.getFullName());
        dashboardView.setGreeting("Good Evening, " + userAccount.getFullName());
        dashboardView.setHeroSubtitle("Ready to solve today's challenge?");
        dashboardView.setProblemsSolved(24);
        dashboardView.setContestsJoined(6);
        dashboardView.setSubmissions(41);
        dashboardView.setCurrentRank("Unranked");
        dashboardView.setActiveContestName("Weekly Sprint Challenge");
        dashboardView.setActiveContestStartTime("Starts at 8:00 PM UTC");
        dashboardView.setActiveContestDuration("48 Hours");

        List<String> recentActivity = new ArrayList<>();
        recentActivity.add("Accepted Two Sum");
        recentActivity.add("Submitted Longest Substring");
        recentActivity.add("Attempted Binary Search");
        dashboardView.setRecentActivity(recentActivity);

        List<String> upcomingContests = new ArrayList<>();
        upcomingContests.add("Code Rush 2026 — Tomorrow");
        upcomingContests.add("Algorithm Sprint — Friday");
        upcomingContests.add("Debugging Arena — Next Week");
        dashboardView.setUpcomingContests(upcomingContests);

        return dashboardView;
    }
}
