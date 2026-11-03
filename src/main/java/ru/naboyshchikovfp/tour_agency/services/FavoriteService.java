package ru.naboyshchikovfp.tour_agency.services;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import ru.naboyshchikovfp.tour_agency.models.UserModel;
import ru.naboyshchikovfp.tour_agency.models.FavoriteModel;
import ru.naboyshchikovfp.tour_agency.models.TourModel;
import ru.naboyshchikovfp.tour_agency.repositories.FavoriteRepository;
import ru.naboyshchikovfp.tour_agency.repositories.TourRepository;
import ru.naboyshchikovfp.tour_agency.dto.TourViewDto;
import ru.naboyshchikovfp.tour_agency.repositories.UserRepository;
import ru.naboyshchikovfp.tour_agency.exceptions.NotFoundException;

@Service 
@Validated 
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, TourRepository tourRepository, UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.tourRepository = tourRepository;
        this.userRepository = userRepository;
    }

    // Добавляет тур в избранное текущего пользователя.
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public void add(@NotNull Long tourId) {
        UserModel user = getCurrentUser();
        TourModel tour = tourRepository.findById(tourId).orElseThrow(() -> new NotFoundException("Тур не найден"));

        if(favoriteRepository.existsByUserAndTour(user, tour)) {
            return;
        }

        favoriteRepository.save(new FavoriteModel(user, tour));

    }

    // Удаляет тур из избранного только у текущего пользователя.
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public void remove(@NotNull Long tourId) {
        UserModel user = getCurrentUser();
        TourModel tour = tourRepository.findById(tourId).orElseThrow(() -> new NotFoundException("Тур не найден"));

        favoriteRepository.findByUserAndTour(user, tour).ifPresent(favorite -> favoriteRepository.delete(favorite));

    }

    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public List<TourViewDto> getMyFavorites() {
        UserModel user = getCurrentUser();

        return favoriteRepository.findByUser(user).stream().map(favorite -> TourViewDto.fromModel(favorite.getTour())).toList();
    }

    // Находит в БД текущего авторизованного пользователя.
    private UserModel getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

}
