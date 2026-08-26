package com.codingplatform.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codingplatform.dto.DashboardView;
import com.codingplatform.model.Submission;
import com.codingplatform.model.UserAccount;
import com.codingplatform.security.CustomUserDetails;
import com.codingplatform.service.DashboardService;
import com.codingplatform.service.SubmissionService;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final SubmissionService submissionService;

    public DashboardServiceImpl(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardView buildDashboardView(Authentication authentication) {
        String userName = "Guest";

        DashboardView view = new DashboardView();
        view.setUserName(userName);
        view.setGreeting("Welcome back, " + userName + "!");
        view.setHeroSubtitle("Track your coding progress and contests.");
        view.setCurrentRank("Unranked");
        view.setActiveContestName(null);

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            UserAccount user = ((CustomUserDetails) authentication.getPrincipal()).getUserAccount();
            userName = user.getFullName();
            int solvedCount = submissionService.countSolvedProblems(user);
            int submissionCount = submissionService.countUserSubmissions(user);
            int acceptedCount = submissionService.countAcceptedSubmissions(user);
            List<String> recentActivity = submissionService.getRecentSubmissions(user)
                    .stream()
                    .map(this::formatActivityEntry)
                    .collect(Collectors.toList());

            view.setUserName(userName);
            view.setGreeting("Welcome back, " + userName + "!");
            view.setProblemsSolved(solvedCount);
            view.setSubmissions(submissionCount);
            view.setAcceptedSolutions(acceptedCount);
            view.setSuccessRate(submissionCount == 0 ? 0 : acceptedCount * 100 / submissionCount);
            view.setRecentActivity(recentActivity.isEmpty()
                    ? List.of("No recent submissions yet.")
                    : recentActivity);
        } else {
            view.setProblemsSolved(0);
            view.setSubmissions(0);
            view.setAcceptedSolutions(0);
            view.setSuccessRate(0);
            view.setRecentActivity(List.of("Welcome to FluxGuard!", "Complete your first challenge."));
        }

        view.setContestsJoined(0);
        view.setUpcomingContests(List.of());
        return view;
    }

    private String formatActivityEntry(Submission submission) {
        String problemTitle = submission.getProblem() != null ? submission.getProblem().getTitle() : "Unknown problem";
        return problemTitle + " — " + submission.getStatus();
    }
}
