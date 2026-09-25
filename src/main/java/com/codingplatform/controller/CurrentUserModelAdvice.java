package com.codingplatform.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.codingplatform.security.CustomUserDetails;

import org.springframework.security.core.Authentication;

@ControllerAdvice
public class CurrentUserModelAdvice {

    @ModelAttribute
    public void addCurrentUserName(Authentication authentication, Model model) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            model.addAttribute("currentUserName", details.getUserAccount().getFullName());
        }
    }
}
