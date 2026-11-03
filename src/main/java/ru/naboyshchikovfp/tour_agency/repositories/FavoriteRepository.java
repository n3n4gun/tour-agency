package ru.naboyshchikovfp.tour_agency.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import ru.naboyshchikovfp.tour_agency.models.FavoriteModel;
import ru.naboyshchikovfp.tour_agency.models.UserModel;
import ru.naboyshchikovfp.tour_agency.models.TourModel;

public interface FavoriteRepository extends JpaRepository<FavoriteModel, Long> {
    @EntityGraph(attributePaths = {"tour", "tour.hotel"})
    List<FavoriteModel> findByUser(UserModel user);

    boolean existsByUserAndTour(UserModel user, TourModel tour);

    Optional<FavoriteModel> findByUserAndTour(UserModel user, TourModel tour);
    
    boolean existsByTourId(Long tourId);
}
