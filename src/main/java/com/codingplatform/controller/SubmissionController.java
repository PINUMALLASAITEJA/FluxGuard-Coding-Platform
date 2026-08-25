package com.codingplatform.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.codingplatform.model.UserAccount;
import com.codingplatform.security.CustomUserDetails;
import com.codingplatform.service.ProblemService;
import com.codingplatform.service.SubmissionService;
import org.springframework.ui.Model;

@Controller
public class SubmissionController {

    private final SubmissionService submissionService;
    private final ProblemService problemService;

    public SubmissionController(SubmissionService submissionService, ProblemService problemService) {
        this.submissionService = submissionService;
        this.problemService = problemService;
    }

    @PostMapping("/submit")
    public String submitSolution(Authentication authentication,
                                 @RequestParam("problemId") Long problemId,
                                 @RequestParam("code") String code,
                                 @RequestParam("language") String language,
                                 Model model) {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            return "redirect:/login";
        }

        UserAccount user = ((CustomUserDetails) principal).getUserAccount();
        try {
            submissionService.submitSolution(user, problemId, code, language);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("problem", problemService.getProblemById(problemId));
            model.addAttribute("problemId", problemId);
            model.addAttribute("code", code);
            model.addAttribute("language", language);
            return "problem-detail";
        }
        return "redirect:/problems";
    }

    @GetMapping("/submissions")
    public String submissionHistory(Authentication authentication, Model model) {
        UserAccount user = getAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("submissions", submissionService.getUserSubmissions(user));
        return "submissions";
    }

    private UserAccount getAuthenticatedUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUserAccount();
        }
        return null;
    }
}
