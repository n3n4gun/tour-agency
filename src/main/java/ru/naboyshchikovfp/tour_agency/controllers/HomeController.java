package ru.naboyshchikovfp.tour_agency.controllers;

import java.security.Principal;
import org.springframework.security.core.Authentication;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller 
public class HomeController {

    // Передаёт email вошедшего пользователя на главную страницу.
    @GetMapping("/")
    public String showHomePage(Model model, Principal principal) {
        String email = principal == null ? null : principal.getName();

        model.addAttribute("currentUserEmail", email);

        // Ссылки выбираются по правам текущей сессии, а не по значению из запроса.
        boolean isAdmin = principal instanceof Authentication authentication
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        boolean isUser = principal instanceof Authentication authentication
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isUser", isUser);

        return "index";
    }

}
