package com.codingplatform.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.codingplatform.dto.DashboardView;
import com.codingplatform.model.Submission;
import com.codingplatform.model.UserAccount;
import com.codingplatform.repository.SubmissionRepository;
import com.codingplatform.security.CustomUserDetails;
import com.codingplatform.service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final SubmissionRepository submissionRepository;

    public DashboardServiceImpl(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @Override
    public DashboardView buildDashboardView(Authentication authentication) {
        String userName = (authentication != null && authentication.getName() != null)
                ? authentication.getName()
                : "Guest";

        DashboardView view = new DashboardView();
        view.setUserName(userName);
        view.setGreeting("Welcome back, " + userName + "!");
        view.setHeroSubtitle("Track your coding progress and contests.");
        view.setCurrentRank("Unranked");
        view.setActiveContestName("No active contest");
        view.setActiveContestStartTime("");
        view.setActiveContestDuration("");

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            UserAccount user = ((CustomUserDetails) authentication.getPrincipal()).getUserAccount();
            int acceptedCount = submissionRepository.countByUserAndStatus(user, "ACCEPTED");
            int submissionCount = submissionRepository.countByUser(user);
            List<String> recentActivity = submissionRepository.findTop5ByUserOrderBySubmittedAtDesc(user)
                    .stream()
                    .map(this::formatActivityEntry)
                    .collect(Collectors.toList());

            view.setProblemsSolved(acceptedCount);
            view.setSubmissions(submissionCount);
            view.setRecentActivity(recentActivity.isEmpty()
                    ? List.of("No recent submissions yet.")
                    : recentActivity);
        } else {
            view.setProblemsSolved(0);
            view.setSubmissions(0);
            view.setRecentActivity(List.of("Welcome to FluxGuard!", "Complete your first challenge."));
        }

        view.setContestsJoined(0);
        view.setUpcomingContests(List.of("No upcoming contests"));
        return view;
    }

    private String formatActivityEntry(Submission submission) {
        String problemTitle = submission.getProblem() != null ? submission.getProblem().getTitle() : "Unknown problem";
        return problemTitle + " — " + submission.getStatus();
    }
}
