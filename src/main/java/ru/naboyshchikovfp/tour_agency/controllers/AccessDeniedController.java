package ru.naboyshchikovfp.tour_agency.controllers;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller 
public class AccessDeniedController {
    // Возвращает страницу с объяснением отказа в доступе.
    @RequestMapping("/access-denied")
    public String accessDenied(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        return "error/403";
    }

}
