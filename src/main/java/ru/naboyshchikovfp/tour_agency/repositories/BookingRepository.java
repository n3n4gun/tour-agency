package ru.naboyshchikovfp.tour_agency.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import ru.naboyshchikovfp.tour_agency.models.BookingModel;
import ru.naboyshchikovfp.tour_agency.models.UserModel;

public interface BookingRepository extends JpaRepository<BookingModel, Long>{
    @EntityGraph(attributePaths = "tour")
    List<BookingModel> findByUserOrderByCreatedAtDesc(UserModel user);

    Optional<BookingModel> findByIdAndUser(Long id, UserModel user);

    @EntityGraph(attributePaths = "tour")
    List<BookingModel> findAllByOrderByCreatedAtDesc();
    
    boolean existsByTourId(Long tourId);
}
