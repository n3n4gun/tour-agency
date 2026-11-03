package ru.naboyshchikovfp.tour_agency.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import ru.naboyshchikovfp.tour_agency.models.BookingModel;
import ru.naboyshchikovfp.tour_agency.models.BookingStatus;
import ru.naboyshchikovfp.tour_agency.models.TourModel;

public record BookingViewDto(
        Long id,
        Long tourId,
        String tourName,
        LocalDate departureDate,
        LocalDateTime createdAt,
        int adultsCount,
        int childrenCount,
        BigDecimal totalPrice,
        BookingStatus status
) {
    // Преобразует бронирование в данные для списка поездок или отдельной страницы.
    public static BookingViewDto fromModel(BookingModel booking) {
        TourModel tour = booking.getTour();

        return new BookingViewDto(
                booking.getId(),
                tour.getId(),
                tour.getTourName(),
                booking.getDepartureDate(),
                booking.getCreatedAt(),
                booking.getAdultsCount(),
                booking.getChildrenCount(),
                booking.getTotalPrice(),
                booking.getStatus()
        );
    }
}