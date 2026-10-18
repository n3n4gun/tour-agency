package ru.naboyshchikovfp.tour_agency.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.naboyshchikovfp.tour_agency.models.HotelModel;

public class HotelDto {
    @NotBlank(message = "Укажите город")
    @Size(min = 2, max = 150, message = "Название города должно быть от 2 до 150 символов")
    private String cityName;

    @NotBlank(message = "Укажите название отеля")
    @Size(min = 2, max = 100, message = "Название отеля должно быть от 2 до 100 символов")
    private String hotelName;

    @NotNull(message = "Укажите звездность")
    @Min(value = 1, message = "Звёздность должна быть от 1 до 5")
    @Max(value = 5, message = "Звёздность должна быть от 1 до 5")
    // тип Integer используется, чтобы при валидации данных в переменную, в крайнем случае, пошло null
    // а не возникала ошибка Spring преобразования типов
    private Integer stars;

    @NotBlank(message = "Укажите адрес отеля")
    @Size(min = 2, max = 255, message = "Адрес отеля должен быть от 2 до 255 символов")
    private String address;

    @Size(max = 5000, message = "Описание не должно превышать 5000 символов")
    private String description;

    public HotelDto() {}

    public static HotelDto fromModel(HotelModel hotel) {
        HotelDto dto = new HotelDto();
        dto.setCityName(hotel.getCityName());
        dto.setHotelName(hotel.getHotelName());
        dto.setStars(hotel.getStars());
        dto.setAddress(hotel.getAddress());
        dto.setDescription(hotel.getDescription());
        return dto;
    }

    public String getCityName() {
        return this.cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getHotelName() {
        return this.hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public Integer getStars() {
        return this.stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
