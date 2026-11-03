package ru.naboyshchikovfp.tour_agency.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.naboyshchikovfp.tour_agency.dto.TourDto;
import ru.naboyshchikovfp.tour_agency.dto.TourViewDto;
import ru.naboyshchikovfp.tour_agency.dto.TourFilterDto;
import ru.naboyshchikovfp.tour_agency.exceptions.ValidationException;
import ru.naboyshchikovfp.tour_agency.exceptions.NotFoundException;
import ru.naboyshchikovfp.tour_agency.models.HotelModel;
import ru.naboyshchikovfp.tour_agency.models.TourModel;
import ru.naboyshchikovfp.tour_agency.repositories.HotelRepository;
import ru.naboyshchikovfp.tour_agency.repositories.TourRepository;
import ru.naboyshchikovfp.tour_agency.repositories.BookingRepository;
import ru.naboyshchikovfp.tour_agency.repositories.FavoriteRepository;

import jakarta.persistence.criteria.Predicate;

@Service 
@Validated 
public class TourService {
    private final TourRepository tourRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final FavoriteRepository favoriteRepository;

    // Передаёт репозитории тура и связанных записей через конструктор.
    public TourService(TourRepository tourRepository, HotelRepository hotelRepository, BookingRepository bookingRepository, FavoriteRepository favoriteRepository) {
        this.tourRepository = tourRepository;
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
        this.favoriteRepository = favoriteRepository;
    }

    // Возвращает данные тура для редактирования менеджером или администратором.
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional(readOnly = true)
    public TourDto getById(Long tourId) {
        TourModel tour = tourRepository.findById(tourId).orElseThrow(() -> new NotFoundException("Тур не найден"));

        return TourDto.fromModel(tour);

    }

    // Создаёт тур с существующим отелем и возвращает его идентификатор.
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional 
    public Long create(@NotNull @Valid TourDto dto) {
        HotelModel hotel = hotelRepository.findById(dto.getHotelId()).orElseThrow(() -> new NotFoundException("Отель не найден"));

        TourModel tour = new TourModel(
            dto.getTourName().trim(),
            dto.getDescription(),
            dto.getTourTypeName().trim(),
            hotel,
            dto.getPricePerAdultPerson(),
            dto.getPricePerChildPerson(),
            dto.getDurationNights()
        );

        return tourRepository.save(tour).getId();
    }

    // Обновляет тур с учётом ограничений для существующих бронирований.
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional 
    public void update(Long tourId, @NotNull @Valid TourDto dto) {
        TourModel tour = tourRepository.findById(tourId).orElseThrow(() -> new NotFoundException("Тур не найден"));
        HotelModel hotel = hotelRepository.findById(dto.getHotelId()).orElseThrow(() -> new NotFoundException("Отель не найден"));

        boolean hotelChanged = !tour.getHotel().getId().equals(hotel.getId());
        boolean durationChanged = tour.getDurationNights() != dto.getDurationNights();

        // При наличии бронирований запрещаем менять отель или количество ночей.
        if ((hotelChanged || durationChanged) && bookingRepository.existsByTourId(tourId)) {
            throw new ValidationException(
                "tourId",
                "Нельзя менять отель или количество ночей тура, по которому уже есть бронирования"
            );
        }

        tour.setTourName(dto.getTourName().trim());
        tour.setDescription(dto.getDescription());
        tour.setTourTypeName(dto.getTourTypeName().trim());
        tour.setHotel(hotel);
        tour.setPricePerAdultPerson(dto.getPricePerAdultPerson());
        tour.setPricePerChildPerson(dto.getPricePerChildPerson());
        tour.setDurationNights(dto.getDurationNights());
    }

    // Возвращает данные тура и отеля для публичной карточки.
    @Transactional(readOnly = true)
    public TourViewDto getDetails(Long tourId) {
        TourModel tour = tourRepository.findById(tourId).orElseThrow(() -> new NotFoundException("Тур не найден"));

        return TourViewDto.fromModel(tour);
    }

    // Проверяет значения фильтра и порядок границ диапазонов.
    private void validateFilter(TourFilterDto filter) {
        // Проверяем, что заданная граница цены не отрицательна.
        if (filter.getMinPrice() != null && filter.getMinPrice().signum() < 0) {
            throw new ValidationException(
                "minPrice",
                "Минимальная цена не может быть отрицательной"
            );
        }


        // Проверяем, что заданная граница цены не отрицательна.
        if (filter.getMaxPrice() != null && filter.getMaxPrice().signum() < 0) {
            throw new ValidationException(
                "maxPrice",
                "Максимальная цена не может быть отрицательной"
            );
        }


        // Минимальная цена не должна превышать максимальную, если заданы обе.
        if (filter.getMinPrice() != null && filter.getMaxPrice() != null && filter.getMinPrice().compareTo(filter.getMaxPrice()) > 0) {
        throw new ValidationException(
                "maxPrice",
                "Максимальная цена не может быть меньше минимальной"
            );
        }


        // Заданное количество ночей должно быть не меньше единицы.
        if (filter.getMinNights() != null && filter.getMinNights() < 1) {
            throw new ValidationException(
                "minNights",
                "Минимальное количество ночей должно быть не меньше 1"
            );
        }


        // Заданное количество ночей должно быть не меньше единицы.
        if (filter.getMaxNights() != null && filter.getMaxNights() < 1) {
            throw new ValidationException(
                "maxNights",
                "Максимальное количество ночей должно быть не меньше 1"
            );
        }


        // Минимальное количество ночей не должно превышать максимальное.
        if (filter.getMinNights() != null && filter.getMaxNights() != null && filter.getMinNights() > filter.getMaxNights()) {
            throw new ValidationException(
                "maxNights",
                "Максимальное количество ночей не может быть меньше минимального"
            );
        }

    }

    // Возвращает страницу туров, соответствующих всем заполненным условиям фильтра.
    @Transactional(readOnly = true)
    public Page<TourViewDto> search(@NotNull TourFilterDto filter, @NotNull Pageable pageable) {
        validateFilter(filter);

        Specification<TourModel> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();


            // Если город указан, добавляем точное сравнение без учёта регистра.
            if (filter.getCityName() != null && !filter.getCityName().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("hotel").get("cityName")),
                        filter.getCityName().trim().toLowerCase(Locale.ROOT)
                ));
            }


            // Если вид тура указан, добавляем сравнение без учёта регистра.
            if (filter.getTourTypeName() != null && !filter.getTourTypeName().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("tourTypeName")),
                        filter.getTourTypeName().trim().toLowerCase(Locale.ROOT)
                ));
            }


            // Добавляем нижнюю границу цены за взрослого, включая указанное значение.
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("pricePerAdultPerson"),
                        filter.getMinPrice()
                ));
            }


            // Добавляем верхнюю границу цены за взрослого, включая указанное значение.
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("pricePerAdultPerson"),
                        filter.getMaxPrice()
                ));
            }


            // Добавляем нижнюю границу количества ночей.
            if (filter.getMinNights() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("durationNights"),
                        filter.getMinNights()
                ));
            }


            // Добавляем верхнюю границу количества ночей.
            if (filter.getMaxNights() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("durationNights"),
                        filter.getMaxNights()
                ));
            }

            // Объединяем условия через И; пустой список не ограничивает выборку.
            return cb.and(predicates.toArray(new Predicate[0]));
        };  

        return tourRepository.findAll(specification, pageable).map(TourViewDto::fromModel);

    }

    // Удаляет тур, если нет связанных бронирований и записей избранного.
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional 
    public void delete(Long tourId) {
        TourModel tour = tourRepository.findById(tourId).orElseThrow(() -> new NotFoundException("Тур не найден"));


        // Сохраняем тур, если по нему есть бронирования любого статуса.
        if (bookingRepository.existsByTourId(tourId)) {
            throw new ValidationException(
                "tourId",
                "Нельзя удалить тур, по которому есть бронирования"
            );
        }


        // Запрещаем удаление тура, сохранённого пользователями в избранном.
        if (favoriteRepository.existsByTourId(tourId)) {
            throw new ValidationException(
                "tourId",
                "Нельзя удалить тур, который находится в избранном"
            );
        }

        tourRepository.delete(tour);
    }

}
