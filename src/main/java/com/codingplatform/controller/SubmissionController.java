package com.codingplatform.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.codingplatform.model.Problem;
import com.codingplatform.model.Submission;
import com.codingplatform.model.UserAccount;
import com.codingplatform.repository.ProblemRepository;
import com.codingplatform.security.CustomUserDetails;
import com.codingplatform.service.SubmissionService;

@Controller
public class SubmissionController {

    private final SubmissionService submissionService;
    private final ProblemRepository problemRepository;

    public SubmissionController(SubmissionService submissionService, ProblemRepository problemRepository) {
        this.submissionService = submissionService;
        this.problemRepository = problemRepository;
    }

    @PostMapping("/submit")
    public String submitSolution(Authentication authentication,
                                 @RequestParam("problemId") Long problemId,
                                 @RequestParam("code") String code,
                                 @RequestParam("language") String language) {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            return "redirect:/login";
        }

        UserAccount user = ((CustomUserDetails) principal).getUserAccount();
        Problem problem = problemRepository.findById(problemId).orElseThrow();

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setProblem(problem);
        submission.setCode(code);
        submission.setLanguage(language);
        submission.setStatus("PENDING");

        submissionService.submit(submission);
        return "redirect:/problems";
    }
}
