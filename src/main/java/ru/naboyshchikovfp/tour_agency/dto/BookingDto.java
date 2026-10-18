package ru.naboyshchikovfp.tour_agency.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// пользователь берется из Spring Security, тур - из адреса запроса,
// итоговая стоимость считается в модели, статус по умолчанию NEW,
// поэтому этих полей в форме нет
public class BookingDto {
    // поле <input type="date"> присылает дату в формате yyyy-MM-dd
    @NotNull(message = "Укажите дату заезда")
    @Future(message = "Дата заезда должна быть в будущем")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate departureDate;

    @NotNull(message = "Укажите количество взрослых")
    @Min(value = 1, message = "Количество взрослых лиц должно быть строго больше 0")
    private Integer adultsCount = 1;

    @NotNull(message = "Укажите количество детей")
    @Min(value = 0, message = "Количество детей не может быть отрицательным")
    private Integer childrenCount = 0;

    public BookingDto() {}

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }
    public LocalDate getDepartureDate() { return this.departureDate; }

    public void setAdultsCount(Integer adultsCount) {
        this.adultsCount = adultsCount;
    }
    public Integer getAdultsCount() { return this.adultsCount; }

    public void setChildrenCount(Integer childrenCount) {
        this.childrenCount = childrenCount;
    }
    public Integer getChildrenCount() { return this.childrenCount; }

}
