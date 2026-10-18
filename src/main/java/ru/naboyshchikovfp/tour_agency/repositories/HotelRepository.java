package ru.naboyshchikovfp.tour_agency.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.naboyshchikovfp.tour_agency.models.HotelModel;

public interface HotelRepository extends JpaRepository<HotelModel, Long> {
    boolean existsByHotelName(String name);
    boolean existsByHotelNameAndIdNot(String name, Long id);
}
