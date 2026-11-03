package ru.naboyshchikovfp.tour_agency.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.naboyshchikovfp.tour_agency.dto.HotelDto;
import ru.naboyshchikovfp.tour_agency.dto.HotelListDto;
import ru.naboyshchikovfp.tour_agency.exceptions.ValidationException;
import ru.naboyshchikovfp.tour_agency.exceptions.NotFoundException;
import ru.naboyshchikovfp.tour_agency.models.HotelModel;
import ru.naboyshchikovfp.tour_agency.repositories.HotelRepository;
import ru.naboyshchikovfp.tour_agency.repositories.TourRepository;

@Service 
@Validated 
public class HotelService {
    private final HotelRepository hotelRepository;
    private final TourRepository tourRepository;

    // Передаёт репозитории отелей и туров через конструктор.
    public HotelService(HotelRepository hotelRepository, TourRepository tourRepository) {
        this.hotelRepository = hotelRepository;
        this.tourRepository = tourRepository;
    }

    // Находит отель и возвращает DTO для формы редактирования.
    @Transactional(readOnly = true)
    public HotelDto getById(Long hotelId) {
        HotelModel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new NotFoundException("Отель не найден"));
        
        return HotelDto.fromModel(hotel);
    }

    // Создаёт отель с уникальным названием и возвращает его идентификатор.
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional 
    public Long create(@NotNull @Valid HotelDto dto) {
        String hotelName = dto.getHotelName().trim();


        // Запрещаем создание отеля с уже существующим названием.
        if (hotelRepository.existsByHotelName(hotelName)) {
            throw new ValidationException(
                "hotelName",
                "Отель с таким названием уже существует"
            );
        }

        HotelModel hotel = new HotelModel(
            dto.getCityName().trim(),
            hotelName,
            dto.getStars(),
            dto.getAddress().trim(),
            dto.getDescription()
        );

        return hotelRepository.save(hotel).getId();

    }

    // Обновляет отель, проверяя уникальность названия среди других записей.
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional 
    public void update(Long hotelId, @NotNull @Valid HotelDto dto) {
        HotelModel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new NotFoundException("Отель не найден"));

        String hotelName = dto.getHotelName().trim();


        // Исключаем текущий отель из проверки, чтобы разрешить прежнее название.
        if (hotelRepository.existsByHotelNameAndIdNot(hotelName, hotelId)) {
            throw new ValidationException(
                "hotelName",
                "Отель с таким названием уже существует"
            );
        }

        hotel.setCityName(dto.getCityName().trim());
        hotel.setHotelName(hotelName);
        hotel.setStars(dto.getStars());
        hotel.setAddress(dto.getAddress().trim());
        hotel.setDescription(dto.getDescription());
    }

    // Возвращает страницу кратких сведений об отелях.
    @Transactional(readOnly = true)
    public Page<HotelListDto> getHotels(Pageable pageable) {
        return hotelRepository.findAll(pageable).map(HotelListDto::fromModel);
    }

    // Удаляет отель, если на него не ссылаются туры.
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional 
    public void delete(Long hotelId) {
        HotelModel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new NotFoundException("Отель не найден"));


        // Запрещаем удаление отеля, который используется в турах.
        if (tourRepository.existsByHotelId(hotelId)) {
            throw new ValidationException(
                "hotelId",
                "Нельзя удалить отель, который используется в турах"
            );
        }

        hotelRepository.delete(hotel);
    }

}
