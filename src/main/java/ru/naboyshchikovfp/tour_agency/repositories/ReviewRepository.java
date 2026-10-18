package ru.naboyshchikovfp.tour_agency.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.naboyshchikovfp.tour_agency.models.ReviewModel;
import ru.naboyshchikovfp.tour_agency.models.TourModel;
import ru.naboyshchikovfp.tour_agency.models.BookingModel;

public interface ReviewRepository extends JpaRepository<ReviewModel, Long> {
    List<ReviewModel> findByTourOrderByCreatedAtDesc(TourModel tour);
    boolean existsByBooking(BookingModel booking);
}
