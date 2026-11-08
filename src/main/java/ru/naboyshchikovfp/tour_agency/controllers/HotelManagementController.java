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

import ru.naboyshchikovfp.tour_agency.services.HotelService;
import ru.naboyshchikovfp.tour_agency.dto.HotelDto;
import ru.naboyshchikovfp.tour_agency.exceptions.ValidationException;

@Controller 
@RequestMapping("/management/hotels")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class HotelManagementController {
    private final HotelService hotelService;

    public HotelManagementController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public String showHotels(@RequestParam(defaultValue = "0") int page, Model model) {
        int pageNumber = Math.max(page, 0);

        model.addAttribute(
            "hotelsPage",
            hotelService.getHotels(PageRequest.of(pageNumber, 10, Sort.by("id").descending()))
        );

        return "management/hotels";

    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("hotelDto", new HotelDto());
        model.addAttribute("editing" ,false);

        return "management/hotel-form";
    }

    // Проверяет форму и создаёт новый отель.
    @PostMapping("/new")
    public String createHotel(@Valid @ModelAttribute("hotelDto") HotelDto dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("editing", false);

        if (bindingResult.hasErrors()) {
            return "management/hotel-form";
        }

        try {
            hotelService.create(dto);
        } catch (ValidationException exception) {
            // Например, название отеля уже занято.
            bindingResult.rejectValue(exception.getField(), "hotel.error", exception.getMessage());

            return "management/hotel-form";
        }

        redirectAttributes.addFlashAttribute(
            "successMessage",
            "Отель добавлен"
        );

        return "redirect:/management/hotels";

    }

    // Заполняем форму данными выбранного отеля
    @GetMapping("/{hotelId}/edit")
    public String showEditForm(@PathVariable("hotelId") Long hotelId, Model model) {
        model.addAttribute("hotelDto", hotelService.getById(hotelId));
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("editing", true);

        return "management/hotel-form";
    }

    // Проверяет форму и сохраняет изменения выбранного отеля.
    @PostMapping("/{hotelId}/edit")
    public String updateHotel(@PathVariable("hotelId") Long hotelId, @Valid @ModelAttribute("hotelDto") HotelDto dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        // При повторном показе формы сохраняем режим редактирования и её адрес.
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("editing", true);

        if (bindingResult.hasErrors()) {
            return "management/hotel-form";
        }

        try {
            hotelService.update(hotelId, dto);
        } catch (ValidationException exception) {
            bindingResult.rejectValue(
                    exception.getField(),
                    "hotel.error",
                    exception.getMessage()
            );

            return "management/hotel-form";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Изменения отеля сохранены"
        );

        return "redirect:/management/hotels";
    }

    @PostMapping("/{hotelId}/delete")
    public String deleteHotel(@PathVariable("hotelId") Long hotelId, RedirectAttributes redirectAttributes) {
        try {
            hotelService.delete(hotelId);
            
            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Отель удалён"
            );
        } catch (ValidationException exception) {
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                exception.getMessage()
            );
        }

        return "redirect:/management/hotels";
    }

}
