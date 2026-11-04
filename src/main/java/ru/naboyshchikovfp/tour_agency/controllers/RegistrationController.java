package ru.naboyshchikovfp.tour_agency.controllers;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import ru.naboyshchikovfp.tour_agency.dto.UserRegistrationDto;
import ru.naboyshchikovfp.tour_agency.exceptions.ValidationException;
import ru.naboyshchikovfp.tour_agency.services.UserService;

@Controller 
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    // Подготавливает пустую форму регистрации и выбирает HTML-шаблон.
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute(
            "userRegistrationDto",
            new UserRegistrationDto()
        );

        return "register";
    }

    @PostMapping("/register")
    public String register(
        @Valid @ModelAttribute("userRegistrationDto")
        UserRegistrationDto dto,
        BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.register(dto);
        } catch (ValidationException exception) {
            bindingResult.rejectValue(exception.getField(), "registration.error", exception.getMessage());

            return "register";
        }

        return "redirect:/login?registered";

    }

}
