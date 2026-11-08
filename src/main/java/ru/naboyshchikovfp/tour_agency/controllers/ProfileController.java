package ru.naboyshchikovfp.tour_agency.controllers;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.naboyshchikovfp.tour_agency.dto.UserProfileDto;
import ru.naboyshchikovfp.tour_agency.services.UserService;

@Controller 
@PreAuthorize("isAuthenticated()")
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    // Заполняет форму текущими данными пользователя.
    @GetMapping("/profile")
    public String showProfile(Model model) {
        model.addAttribute("userProfileDto", userService.getMyProfile());

        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("userProfileDto") UserProfileDto dto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "profile";
        }

        userService.updateMyProfile(dto);

        redirectAttributes.addFlashAttribute(
            "successMessage",
            "Профиль обновлен"
        );

        return "redirect:/profile";

    }

}
