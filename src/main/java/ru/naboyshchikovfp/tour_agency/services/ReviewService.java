package ru.naboyshchikovfp.tour_agency.services;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import ru.naboyshchikovfp.tour_agency.dto.ReviewDto;
import ru.naboyshchikovfp.tour_agency.dto.ReviewViewDto;
import ru.naboyshchikovfp.tour_agency.exceptions.NotFoundException;
import ru.naboyshchikovfp.tour_agency.exceptions.ValidationException;
import ru.naboyshchikovfp.tour_agency.models.BookingModel;
import ru.naboyshchikovfp.tour_agency.models.BookingStatus;
import ru.naboyshchikovfp.tour_agency.models.ReviewModel;
import ru.naboyshchikovfp.tour_agency.models.UserModel;
import ru.naboyshchikovfp.tour_agency.models.TourModel;
import ru.naboyshchikovfp.tour_agency.repositories.BookingRepository;
import ru.naboyshchikovfp.tour_agency.repositories.ReviewRepository;
import ru.naboyshchikovfp.tour_agency.repositories.UserRepository;
import ru.naboyshchikovfp.tour_agency.repositories.TourRepository;

@Service 
@Validated 
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository, UserRepository userRepository, TourRepository tourRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.tourRepository = tourRepository;
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public Long create(@NotNull Long bookingId, @NotNull @Valid ReviewDto dto) {
        UserModel user = getCurrentUser();
        BookingModel booking = bookingRepository.findByIdAndUser(bookingId, user).orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        // Отзыв разрешён только после завершения поездки.
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ValidationException(
                    "bookingId",
                    "Отзыв можно оставить только после завершённой поездки"
            );
        }

        // На одно бронирование допускается только один отзыв.
        if (reviewRepository.existsByBooking(booking)) {
            throw new ValidationException(
                    "bookingId",
                    "Отзыв по этой поездке уже оставлен"
            );
        }

        ReviewModel review = new ReviewModel(
            booking,
            dto.getRating(),
            dto.getText()
        );

        return reviewRepository.save(review).getId();

    }

    // Возвращает отзывы тура, начиная с самых новых.
    @Transactional(readOnly = true)
    public List<ReviewViewDto> getByTour(@NotNull Long tourId) {
        TourModel tour = tourRepository.findById(tourId).orElseThrow(() -> new NotFoundException("Тур не найден"));

        return reviewRepository.findByTourOrderByCreatedAtDesc(tour).stream().map(ReviewViewDto::fromModel).toList();
    }

    // Находит в БД текущего авторизованного пользователя.
    private UserModel getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

}
