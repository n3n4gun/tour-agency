package ru.naboyshchikovfp.tour_agency.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import ru.naboyshchikovfp.tour_agency.models.TourModel;

public interface TourRepository extends JpaRepository<TourModel, Long>, JpaSpecificationExecutor<TourModel> {
    boolean existsByHotelId(Long hotelId);

    @Override 
    @EntityGraph(attributePaths = "hotel")
    Page<TourModel> findAll(Specification<TourModel> specification, Pageable pageable);
    
}
