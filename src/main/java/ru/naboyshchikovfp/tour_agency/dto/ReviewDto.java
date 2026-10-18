package ru.naboyshchikovfp.tour_agency.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// бронирование, по которому оставляется отзыв, берется из адреса запроса,
// принадлежность бронирования текущему пользователю проверяет сервис
public class ReviewDto {
    @NotNull(message = "Укажите оценку")
    @Min(value = 1, message = "Оценка должна быть от 1 до 5")
    @Max(value = 5, message = "Оценка должна быть от 1 до 5")
    private Integer rating;

    @Size(max = 5000, message = "Текст отзыва не должен превышать 5000 символов")
    private String text;

    public ReviewDto() {}

    public void setRating(Integer rating) {
        this.rating = rating;
    }
    public Integer getRating() { return this.rating; }

    public void setText(String text) {
        this.text = text;
    }
    public String getText() { return this.text; }

}
