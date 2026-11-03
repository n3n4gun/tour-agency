package ru.naboyshchikovfp.tour_agency.dto;

import ru.naboyshchikovfp.tour_agency.models.HotelModel;

public record HotelListDto(
        Long id,
        String hotelName,
        String cityName,
        int stars
) {
    public static HotelListDto fromModel(HotelModel hotel) {
        return new HotelListDto(
                hotel.getId(),
                hotel.getHotelName(),
                hotel.getCityName(),
                hotel.getStars()
        );
    }
}