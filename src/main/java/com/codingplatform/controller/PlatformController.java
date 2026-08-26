package com.codingplatform.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.codingplatform.model.UserAccount;
import com.codingplatform.repository.SubmissionRepository;
import com.codingplatform.repository.UserAccountRepository;
import com.codingplatform.security.CustomUserDetails;
import com.codingplatform.service.SubmissionService;

@Controller
public class PlatformController {

    private final UserAccountRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionService submissionService;

    public PlatformController(UserAccountRepository userRepository, SubmissionRepository submissionRepository,
                              SubmissionService submissionService) {
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.submissionService = submissionService;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        UserAccount user = currentUser(authentication);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        model.addAttribute("submissions", submissionService.getUserSubmissions(user));
        model.addAttribute("solvedCount", submissionService.countSolvedProblems(user));
        model.addAttribute("submissionCount", submissionService.countUserSubmissions(user));
        model.addAttribute("acceptedCount", submissionService.countAcceptedSubmissions(user));
        return "profile";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        List<Map<String, Object>> entries = userRepository.findAll().stream()
                .map(user -> Map.<String, Object>of("user", user, "acceptedProblems",
                        submissionRepository.countDistinctProblemsByUserAndStatus(user, "ACCEPTED"),
                        "acceptedSubmissions", submissionRepository.countByUserAndStatus(user, "ACCEPTED"),
                        "joined", user.getCreatedAt()))
                .sorted(Comparator.<Map<String, Object>>comparingInt(entry -> -((Integer) entry.get("acceptedProblems")))
                        .thenComparingInt(entry -> -((Integer) entry.get("acceptedSubmissions")))
                        .thenComparing(entry -> (java.time.LocalDateTime) entry.get("joined")))
                .toList();
        model.addAttribute("entries", entries);
        return "leaderboard";
    }

    @GetMapping("/contests")
    public String contests(Model model) {
        model.addAttribute("contests", List.of());
        return "contests";
    }

    private UserAccount currentUser(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details
                ? details.getUserAccount() : null;
    }
}