package com.codingplatform.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.codingplatform.model.Problem;
import com.codingplatform.service.ProblemService;

@Controller
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping("/problems")
    public String listProblems(Model model) {
        List<Problem> problems = problemService.getAllProblems();
        model.addAttribute("problems", problems);
        return "problems.html";
    }
}
