package ru.naboyshchikovfp.tour_agency.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.naboyshchikovfp.tour_agency.models.UserModel;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByEmail(String email);
    boolean existsByEmail(String email);
}
