package ru.naboyshchikovfp.tour_agency.controllers;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;

import ru.naboyshchikovfp.tour_agency.dto.TourFilterDto;
import ru.naboyshchikovfp.tour_agency.services.TourService;
import ru.naboyshchikovfp.tour_agency.dto.TourDto;
import ru.naboyshchikovfp.tour_agency.services.HotelService;
import ru.naboyshchikovfp.tour_agency.exceptions.ValidationException;

@Controller
@RequestMapping("/management/tours")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class TourManagementController {
    private final TourService tourService;
    private final HotelService hotelService;

    public TourManagementController(TourService tourService, HotelService hotelService) {
        this.tourService = tourService;
        this.hotelService = hotelService;
    }

    @GetMapping
    public String showTours(@RequestParam(defaultValue = "0") int page, Model model) {
        int pageNumber = Math.max(page, 0);

        model.addAttribute("toursPage", tourService.search(new TourFilterDto(), PageRequest.of(pageNumber, 10, Sort.by("id").descending())));

        return "management/tours";

    }

    // Подготавливает форму создания тура и список доступных отелей.
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("tourDto", new TourDto());
        model.addAttribute("editing", false);

        model.addAttribute(
            "hotels",
            hotelService.getHotels(PageRequest.of(0, 100, Sort.by("hotelName").ascending())).getContent()
        );

        return "management/tour-form";

    }

    // Проверяет форму и создаёт тур с выбранным отелем.
    @PostMapping("/new")
    public String createTour(@Valid @ModelAttribute("tourDto") TourDto dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("hotels", hotelService.getHotels(PageRequest.of(0, 100, Sort.by("hotelName").ascending())).getContent());
        model.addAttribute("editing", false);

        if (bindingResult.hasErrors()) {
            return "management/tour-form";
        }

        tourService.create(dto);

        redirectAttributes.addFlashAttribute(
            "successMessage",
            "Тур добавлен"
        );

        return "redirect:/management/tours";

    }

    // Заполняет форму данными существующего тура.
    @GetMapping("/{tourId}/edit")
    public String showEditForm(@PathVariable("tourId") Long tourId, Model model) {
        model.addAttribute("tourDto", tourService.getById(tourId));
        model.addAttribute("tourId", tourId);
        model.addAttribute("editing", true);

        model.addAttribute(
            "hotels",
            hotelService.getHotels(PageRequest.of(0, 100, Sort.by("hotelName").ascending())).getContent()
        );

        return "management/tour-form";

    }

    // Проверяет форму и сохраняет изменения тура.
    @PostMapping("/{tourId}/edit")
    public String updateTour(@PathVariable("tourId") Long tourId, @Valid @ModelAttribute("tourDto") TourDto dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("tourId", tourId);
        model.addAttribute("editing", true);
        model.addAttribute("hotels", hotelService.getHotels(PageRequest.of(0, 100, Sort.by("hotelName").ascending())).getContent());

        if (bindingResult.hasErrors()) {
            return "management/tour-form";
        }

        try {
            tourService.update(tourId, dto);
        } catch (ValidationException exception) {
            // Ограничение относится к туру в целом.
            bindingResult.reject(
                    "tour.error",
                    exception.getMessage()
            );

            return "management/tour-form";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Изменения тура сохранены"
        );

        return "redirect:/management/tours";
    }

    // Удаление тура
    @PostMapping("/{tourId}/delete")
    public String deleteTour(@PathVariable("tourId") Long tourId, RedirectAttributes redirectAttributes) {
        try {
            tourService.delete(tourId);

            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Тур удалён"
            );
        } catch (ValidationException exception) {
            // Причину запрета показываем на странице списка.
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                exception.getMessage()
            );
        }

        return "redirect:/management/tours";
    }

}
