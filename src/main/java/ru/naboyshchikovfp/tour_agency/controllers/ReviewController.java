package ru.naboyshchikovfp.tour_agency.controllers;

import jakarta.validation.Valid;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import ru.naboyshchikovfp.tour_agency.dto.BookingViewDto;
import ru.naboyshchikovfp.tour_agency.dto.ReviewDto;
import ru.naboyshchikovfp.tour_agency.models.BookingStatus;
import ru.naboyshchikovfp.tour_agency.services.BookingService;
import ru.naboyshchikovfp.tour_agency.exceptions.ValidationException;
import ru.naboyshchikovfp.tour_agency.services.ReviewService;

@Controller 
@PreAuthorize("hasRole('USER')")
public class ReviewController {
    private final BookingService bookingService;
    private final ReviewService reviewService;

    public ReviewController(BookingService bookingService, ReviewService reviewService) {
        this.bookingService = bookingService;
        this.reviewService = reviewService;
    }

    // Открывает форму отзыва по собственной завершённой поездке.
    @GetMapping("/bookings/{bookingId}/review")
    public String showReviewForm(@PathVariable("bookingId") Long bookingId, Model model) {
        BookingViewDto booking = bookingService.getMyBooking(bookingId);

        // До завершения поездки форму отзыва открывать нельзя.
        if (booking.status() != BookingStatus.COMPLETED) {
            return "redirect:/bookings?reviewUnavailable";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("reviewDto", new ReviewDto());

        return "review-form";

    }

    // Проверяет форму и сохраняет отзыв о собственной завершённой поездке.
    @PostMapping("/bookings/{bookingId}/review")
    public String createReview(@PathVariable("bookingId") Long bookingId, @Valid @ModelAttribute("reviewDto") ReviewDto dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        BookingViewDto booking = bookingService.getMyBooking(bookingId);

        // Повторно проверяем завершённость поездки при отправке формы.
        if (booking.status() != BookingStatus.COMPLETED) {
            return "redirect:/bookings?reviewUnavailable";
        }

        // Данные поездки нужны шаблону при повторном показе формы.
        model.addAttribute("booking", booking);

        if (bindingResult.hasErrors()) {
            return "review-form";
        }

        try {
            reviewService.create(bookingId, dto);
        } catch (ValidationException exception) {
            bindingResult.reject(
                "review.error",
                exception.getMessage()
            );

            return "review-form";
        }

        redirectAttributes.addFlashAttribute(
            "successMessage",
            "Отзыв опубликован"
        );

        return "redirect:/bookings";

    }

}
