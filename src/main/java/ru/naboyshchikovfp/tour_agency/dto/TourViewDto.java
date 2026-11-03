package ru.naboyshchikovfp.tour_agency.dto;

import java.math.BigDecimal;

import ru.naboyshchikovfp.tour_agency.models.HotelModel;
import ru.naboyshchikovfp.tour_agency.models.TourModel;

public record TourViewDto(
        Long id,
        String tourName,
        String description,
        String tourTypeName,
        Long hotelId,
        String hotelName,
        String cityName,
        int hotelStars,
        BigDecimal pricePerAdultPerson,
        BigDecimal pricePerChildPerson,
        int durationNights
) {
    public static TourViewDto fromModel(TourModel tour) {
        HotelModel hotel = tour.getHotel();

        return new TourViewDto(
                tour.getId(),
                tour.getTourName(),
                tour.getDescription(),
                tour.getTourTypeName(),
                hotel.getId(),
                hotel.getHotelName(),
                hotel.getCityName(),
                hotel.getStars(),
                tour.getPricePerAdultPerson(),
                tour.getPricePerChildPerson(),
                tour.getDurationNights()
        );
    }
}