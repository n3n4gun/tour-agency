package ru.naboyshchikovfp.tour_agency.support;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import ru.naboyshchikovfp.tour_agency.dto.*;
import ru.naboyshchikovfp.tour_agency.models.*;

/** Общие данные тестов: даты всегда рассчитываются относительно дня запуска. */
public final class TestData {
    private TestData() {}

    public static UserModel user() {
        return new UserModel("client@example.com", "stored-hash", "Ivan", "Petrov", null, true);
    }

    public static HotelModel hotel() {
        return new HotelModel("Sochi", "Seaside", 4, "Central street 10", "Pool");
    }

    public static TourModel tour() {
        return new TourModel("Sea holiday", "Description", "Beach", hotel(),
                new BigDecimal("10000.00"), new BigDecimal("4000.00"), 5);
    }

    public static BookingModel booking(BookingStatus status, LocalDate departure) {
        BookingModel booking = new BookingModel(user(), tour(), departure, 2, 1);
        booking.setStatus(status);
        return booking;
    }

    public static UserRegistrationDto registration() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("CLIENT@Example.com");
        dto.setPassword("Password123!");
        dto.setConfirmPassword("Password123!");
        dto.setFirstName(" Ivan ");
        dto.setLastName(" Petrov ");
        dto.setPhone("");
        return dto;
    }

    public static HotelDto hotelDto() {
        HotelDto dto = new HotelDto();
        dto.setCityName(" Sochi ");
        dto.setHotelName(" Seaside ");
        dto.setStars(4);
        dto.setAddress(" Central street 10 ");
        dto.setDescription("Pool");
        return dto;
    }

    public static TourDto tourDto(Long hotelId) {
        TourDto dto = new TourDto();
        dto.setTourName(" Sea holiday ");
        dto.setDescription("Description");
        dto.setTourTypeName(" Beach ");
        dto.setHotelId(hotelId);
        dto.setPricePerAdultPerson(new BigDecimal("10000.00"));
        dto.setPricePerChildPerson(new BigDecimal("4000.00"));
        dto.setDurationNights(5);
        return dto;
    }

    public static BookingDto bookingDto() {
        BookingDto dto = new BookingDto();
        dto.setDepartureDate(LocalDate.now().plusDays(30));
        dto.setAdultsCount(2);
        dto.setChildrenCount(1);
        return dto;
    }

    public static ReviewDto reviewDto() {
        ReviewDto dto = new ReviewDto();
        dto.setRating(5);
        dto.setText("Excellent trip");
        return dto;
    }

    public static void authenticate(String email, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, "unused",
                        AuthorityUtils.createAuthorityList("ROLE_" + role)));
    }
}
