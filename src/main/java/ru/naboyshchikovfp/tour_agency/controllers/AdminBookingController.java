package ru.naboyshchikovfp.tour_agency.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.naboyshchikovfp.tour_agency.exceptions.ValidationException;
import ru.naboyshchikovfp.tour_agency.services.BookingService;

@Controller
@RequestMapping("/admin/bookings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookingController {
    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Показывает администратору бронирования всех клиентов.
    @GetMapping
    public String showBookings(Model model) {
        model.addAttribute("bookings", bookingService.getAllBookings());

        return "admin/bookings";
    }

    // Подтверждает новую заявку; допустимость статуса и даты проверяет сервис.
    @PostMapping("/{bookingId}/confirm")
    public String confirmBooking(
            @PathVariable("bookingId") Long bookingId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            bookingService.confirmBooking(bookingId);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Бронирование №" + bookingId + " подтверждено"
            );
        } catch (ValidationException exception) {
            // После перенаправления объясняем, почему действие запрещено.
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/admin/bookings";
    }

    // Завершает оплаченную поездку, если наступила дата её окончания.
    @PostMapping("/{bookingId}/complete")
    public String completeBooking(
            @PathVariable("bookingId") Long bookingId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            bookingService.completeBooking(bookingId);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Поездка по бронированию №" + bookingId + " завершена"
            );
        } catch (ValidationException exception) {
            // Отображаем ошибку сервиса на странице списка бронирований.
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/admin/bookings";
    }
}
