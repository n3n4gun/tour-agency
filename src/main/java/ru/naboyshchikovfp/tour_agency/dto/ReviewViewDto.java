package ru.naboyshchikovfp.tour_agency.dto;

import java.time.LocalDateTime;

import ru.naboyshchikovfp.tour_agency.models.ReviewModel;

public record ReviewViewDto(
        Long id,
        String authorName,
        int rating,
        String text,
        LocalDateTime createdAt
) {
    // Преобразует отзыв в данные для публичного отображения.
    public static ReviewViewDto fromModel(ReviewModel review) {
        return new ReviewViewDto(
                review.getId(),
                review.getUser().getFirstName(),
                review.getRating(),
                review.getText(),
                review.getCreatedAt()
        );
    }
}