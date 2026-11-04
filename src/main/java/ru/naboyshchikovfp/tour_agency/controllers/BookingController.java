package ru.naboyshchikovfp.tour_agency.controllers;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.naboyshchikovfp.tour_agency.exceptions.ValidationException;
import ru.naboyshchikovfp.tour_agency.dto.BookingDto;
import ru.naboyshchikovfp.tour_agency.services.TourService;
import ru.naboyshchikovfp.tour_agency.services.BookingService;

@Controller 
public class BookingController {
    private final TourService tourService;
    private final BookingService bookingService;

    public BookingController(TourService tourService, BookingService bookingService) {
        this.tourService = tourService;
        this.bookingService = bookingService;
    }

    // Подготавливает форму бронирования выбранного тура.
    @GetMapping("/bookings/new/{tourId}")
    public String showBookingForm(@PathVariable("tourId") Long tourId, Model model) {
        model.addAttribute("tour", tourService.getDetails(tourId));
        model.addAttribute("bookingDto", new BookingDto());

        return "booking-form";

    }

    // Проверяет параметры поездки и создаёт бронирование текущего пользователя.
    @PostMapping("/bookings/new/{tourId}")
    public String createBooking(@PathVariable("tourId") Long tourId, @Valid @ModelAttribute("bookingDto") BookingDto dto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tour", tourService.getDetails(tourId));

            return "booking-form";

        }

        bookingService.create(tourId, dto);

        return "redirect:/bookings?created";
    }

    // Показывает бронирования текущего пользователя.
    @GetMapping("/bookings")
    public String showMyBookings(Model model) {
        model.addAttribute("bookings", bookingService.getMyBookings());

        return "bookings";
    }

    // Имитирует оплату своей подтверждённой брони; статус, дату и владельца проверяет сервис.
    @PostMapping("/bookings/{bookingId}/pay")
    public String payBooking(
            @PathVariable("bookingId") Long bookingId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            bookingService.payMyBooking(bookingId);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Учебная оплата выполнена. Бронирование оплачено, деньги не списывались."
            );
        } catch (ValidationException exception) {
            // Ошибку статуса или даты показываем после возврата к списку поездок.
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/bookings";
    }

    // Отменяет собственное бронирование и возвращает пользователя к списку.
    @PostMapping("/bookings/{bookingId}/cancel")
    public String cancelBooking(
            @PathVariable("bookingId") Long bookingId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            bookingService.cancelMyBooking(bookingId);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Бронирование отменено"
            );
        } catch (ValidationException exception) {
            // Показываем причину, по которой отмена запрещена.
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }
    
        return "redirect:/bookings";
    }


}
