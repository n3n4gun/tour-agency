package ru.naboyshchikovfp.tour_agency.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.naboyshchikovfp.tour_agency.models.TourModel;

public class TourDto {
    @NotBlank(message = "Укажите название тура")
    @Size(min = 2, max = 100, message = "Название тура должно быть от 2 до 100 символов")
    private String tourName;

    @Size(max = 5000, message = "Описание не должно превышать 5000 символов")
    private String description;

    @NotBlank(message = "Укажите вид тура")
    @Size(max = 50, message = "Вид тура не должен превышать 50 символов")
    private String tourTypeName;

    // из выпадающего списка в форме приходит только id отеля,
    // сам отель сервис находит через репозиторий
    @NotNull(message = "Выберите отель")
    private Long hotelId;

    @NotNull(message = "Укажите стоимость для взрослого")
    @DecimalMin(value = "0.01", message = "Стоимость для взрослого должна быть больше 0")
    @Digits(integer = 8, fraction = 2, message = "Некорректная стоимость")
    private BigDecimal pricePerAdultPerson;

    @NotNull(message = "Укажите стоимость для ребенка")
    @DecimalMin(value = "0.00", message = "Стоимость для ребенка не может быть отрицательной")
    @Digits(integer = 8, fraction = 2, message = "Некорректная стоимость")
    private BigDecimal pricePerChildPerson;

    @NotNull(message = "Укажите количество ночей")
    @Min(value = 1, message = "Минимально допустимое количество ночей составляет 1")
    private Integer durationNights;

    public TourDto() {}

    public static TourDto fromModel(TourModel tour) {
        TourDto dto = new TourDto();
        dto.setTourName(tour.getTourName());
        dto.setDescription(tour.getDescription());
        dto.setTourTypeName(tour.getTourTypeName());
        dto.setHotelId(tour.getHotel().getId());
        dto.setPricePerAdultPerson(tour.getPricePerAdultPerson());
        dto.setPricePerChildPerson(tour.getPricePerChildPerson());
        dto.setDurationNights(tour.getDurationNights());
        return dto;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }
    public String getTourName() { return this.tourName; }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() { return this.description; }

    public void setTourTypeName(String tourTypeName) {
        this.tourTypeName = tourTypeName;
    }
    public String getTourTypeName() { return this.tourTypeName; }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }
    public Long getHotelId() { return this.hotelId; }

    public void setPricePerAdultPerson(BigDecimal pricePerAdultPerson) {
        this.pricePerAdultPerson = pricePerAdultPerson;
    }
    public BigDecimal getPricePerAdultPerson() { return this.pricePerAdultPerson; }

    public void setPricePerChildPerson(BigDecimal pricePerChildPerson) {
        this.pricePerChildPerson = pricePerChildPerson;
    }
    public BigDecimal getPricePerChildPerson() { return this.pricePerChildPerson; }

    public void setDurationNights(Integer durationNights) {
        this.durationNights = durationNights;
    }
    public Integer getDurationNights() { return this.durationNights; }

}
