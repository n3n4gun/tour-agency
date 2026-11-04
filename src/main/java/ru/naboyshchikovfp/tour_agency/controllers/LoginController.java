package ru.naboyshchikovfp.tour_agency.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller 
public class LoginController {

    // Показывает собственную страницу входа.
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

}
