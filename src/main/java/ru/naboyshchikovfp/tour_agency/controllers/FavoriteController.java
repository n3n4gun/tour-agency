package ru.naboyshchikovfp.tour_agency.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.naboyshchikovfp.tour_agency.services.FavoriteService;

@Controller 
@PreAuthorize("hasRole('USER')")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping("/favorites")
    public String showFavorites(Model model) {
        model.addAttribute(
            "tours",
            favoriteService.getMyFavorites()
        );

        return "favorites";

    }

    // Добавляет выбранный тур в избранное текущего пользователя.
    @PostMapping("/favorites/{tourId}/add")
    public String addFavorite(@PathVariable("tourId") Long tourId, RedirectAttributes redirectAttributes) {
        favoriteService.add(tourId);
        
        redirectAttributes.addFlashAttribute(
            "successMessage",
            "Тур добавлен в избранное"
        );

        return "redirect:/favorites";

    }

    // Удаляет выбранный тур из избранного текущего пользователя.
    @PostMapping("/favorites/{tourId}/remove")
    public String removeFavorite(@PathVariable("tourId") Long tourId, RedirectAttributes redirectAttributes) {
        favoriteService.remove(tourId);

        redirectAttributes.addFlashAttribute(
            "successMessage",
            "Тур удалён из избранного"
        );

        return "redirect:/favorites";

    }

}
