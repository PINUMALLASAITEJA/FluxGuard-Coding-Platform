package com.codingplatform.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.codingplatform.model.Problem;
import com.codingplatform.service.ProblemService;
import com.codingplatform.service.ExercismImportService;

@Controller
public class ProblemController {

    private final ProblemService problemService;
    private final ExercismImportService exercismImportService;

    public ProblemController(ProblemService problemService, ExercismImportService exercismImportService) {
        this.problemService = problemService;
        this.exercismImportService = exercismImportService;
    }

    @GetMapping("/problems")
    public String listProblems(Model model) {
        List<Problem> problems = problemService.getAllProblems();
        model.addAttribute("problems", problems);
        return "problems";
    }

    @PostMapping("/problems/import-exercism")
    public String importExercismProblems(Model model) {
        try {
            model.addAttribute("importMessage", "Imported " + exercismImportService.importProblems()
                    + " Exercism problems.");
        } catch (IllegalStateException exception) {
            model.addAttribute("importMessage", exception.getMessage());
        }
        model.addAttribute("problems", problemService.getAllProblems());
        return "problems";
    }

    @GetMapping("/problems/{id}")
    public String problemDetails(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("problem", problemService.getProblemById(id));
            return "problem-detail";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return "redirect:/problems";
        }
    }

    @GetMapping("/problems/new")
    public String newProblem(Model model) {
        model.addAttribute("problem", new Problem());
        return "problem-form";
    }

    @PostMapping("/problems")
    public String createProblem(Problem problem, Model model) {
        return saveProblem(problem, model);
    }

    @GetMapping("/problems/edit/{id}")
    public String editProblem(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("problem", problemService.getProblemById(id));
            return "problem-form";
        } catch (IllegalArgumentException exception) {
            return "redirect:/problems";
        }
    }

    @PostMapping("/problems/update/{id}")
    public String updateProblem(@PathVariable Long id, Problem problem, Model model) {
        try {
            problem.setId(id);
            problemService.saveProblem(problem);
            return "redirect:/problems/" + id;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return "problem-form";
        }
    }

    @PostMapping("/problems/delete/{id}")
    public String deleteProblem(@PathVariable Long id) {
        try {
            problemService.deleteProblem(id);
        } catch (IllegalArgumentException ignored) {
            // Deleting an already removed problem is idempotent for the UI.
        }
        return "redirect:/problems";
    }

    private String saveProblem(Problem problem, Model model) {
        if (problem.getTitle() == null || problem.getTitle().isBlank()
                || problem.getDifficulty() == null || problem.getDifficulty().isBlank()) {
            model.addAttribute("errorMessage", "Title and difficulty are required.");
            return "problem-form";
        }
        problemService.saveProblem(problem);
        return "redirect:/problems/" + problem.getId();
    }
}
