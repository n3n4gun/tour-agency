package ru.naboyshchikovfp.tour_agency.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import ru.naboyshchikovfp.tour_agency.models.TourModel;

public interface TourRepository extends JpaRepository<TourModel, Long>,
                                        JpaSpecificationExecutor<TourModel> {

}
