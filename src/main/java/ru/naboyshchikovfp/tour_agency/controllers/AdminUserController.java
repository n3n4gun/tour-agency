package ru.naboyshchikovfp.tour_agency.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.naboyshchikovfp.tour_agency.services.UserService;
import ru.naboyshchikovfp.tour_agency.exceptions.ValidationException;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // Показывает список пользователей с разбивкой на страницы.
    @GetMapping
    public String showUsers(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by("id").ascending());

        model.addAttribute("userPage", userService.getUsers(pageable));

        return "admin/users";

    }

    // Изменяет доступ пользователя и возвращает администратора к списку.
    @PostMapping("/{userId}/enabled")
    public String setEnabled(@PathVariable("userId") Long userId, @RequestParam("enabled") boolean enabled, RedirectAttributes redirectAttributes) {
        try {
            userService.setEnabled(userId, enabled);

            redirectAttributes.addFlashAttribute(
                "successMessage",
                enabled ? "Пользователь разблокирован" : "Пользователь заблокирован"
            );
        } catch (ValidationException exception) {
            // Показываем причину, по которой сервис запретил изменение.
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                exception.getMessage()
            );
        }

        return "redirect:/admin/users";

    }

}
