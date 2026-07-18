package com.codingplatform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.codingplatform.dto.RegisterRequest;
import com.codingplatform.service.UserAccountService;

import jakarta.validation.Valid;

@Controller
public class RegistrationController {

    private final UserAccountService userAccountService;

    public RegistrationController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userAccountService.registerUser(registerRequest);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
    }
}