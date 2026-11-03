package ru.naboyshchikovfp.tour_agency.services;

import java.util.List;
import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.naboyshchikovfp.tour_agency.dto.BookingDto;
import ru.naboyshchikovfp.tour_agency.dto.BookingViewDto;
import ru.naboyshchikovfp.tour_agency.exceptions.ValidationException;
import ru.naboyshchikovfp.tour_agency.exceptions.NotFoundException;
import ru.naboyshchikovfp.tour_agency.models.BookingModel;
import ru.naboyshchikovfp.tour_agency.models.TourModel;
import ru.naboyshchikovfp.tour_agency.models.UserModel;
import ru.naboyshchikovfp.tour_agency.models.BookingStatus;
import ru.naboyshchikovfp.tour_agency.repositories.BookingRepository;
import ru.naboyshchikovfp.tour_agency.repositories.TourRepository;
import ru.naboyshchikovfp.tour_agency.repositories.UserRepository;

@Service 
@Validated 
public class BookingService {
    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, TourRepository tourRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.tourRepository = tourRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional 
    public Long create(@NotNull Long tourId, @NotNull @Valid BookingDto dto) {
        UserModel user = getCurrentUser();
        TourModel tour = tourRepository.findById(tourId).orElseThrow(() -> new NotFoundException("Тур не найден"));
        BookingModel booking = new BookingModel(
            user,
            tour,
            dto.getDepartureDate(),
            dto.getAdultsCount(),
            dto.getChildrenCount()
        );

        return bookingRepository.save(booking).getId();
    }

    // Возвращает бронирования текущего пользователя, начиная с самых новых.
    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public List<BookingViewDto> getMyBookings() {
        UserModel user = getCurrentUser();

        return bookingRepository.findByUserOrderByCreatedAtDesc(user).stream().map(BookingViewDto::fromModel).toList();
    }

    // Возвращает бронирование, только если оно принадлежит текущему пользователю
    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public BookingViewDto getMyBooking(@NotNull Long bookingId) {
        UserModel user = getCurrentUser();
        BookingModel booking = bookingRepository.findByIdAndUser(bookingId, user).orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        return BookingViewDto.fromModel(booking);
    }

    // Отменяет собственное неоплаченное бронирование до даты отправления.
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public void cancelMyBooking(@NotNull Long bookingId) {
        UserModel user = getCurrentUser();
        BookingModel booking = bookingRepository.findByIdAndUser(bookingId, user).orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (booking.getStatus() != BookingStatus.NEW && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ValidationException(
                "status",
                "Отменить можно только новое или подтверждённое бронирование"
            );
        }

        // В день отправления и после него самостоятельная отмена запрещена.
        if (!booking.getDepartureDate().isAfter(LocalDate.now())) {
            throw new ValidationException(
                "departureDate",
                "Отменить бронирование можно только до даты отправления"
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional(readOnly = true)
    public List<BookingViewDto> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc().stream().map(BookingViewDto::fromModel).toList();
    }

    // Позволяет сотруднику подтвердить новую заявку до даты отправления.
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional
    public void confirmBooking(@NotNull Long bookingId) {
        BookingModel booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        // Подтверждать можно только новые заявки.
        if (booking.getStatus() != BookingStatus.NEW) {
            throw new ValidationException(
                "status",
                "Подтвердить можно только новое бронирование"
            );
        }

        // Не подтверждаем заявки в день отправления или после него.
        if (!booking.getDepartureDate().isAfter(LocalDate.now())) {
            throw new ValidationException(
                "departureDate",
                "Подтвердить бронирование можно только до даты отправления"
            );
        }

        booking.setStatus(BookingStatus.CONFIRMED);

    }

    // Имитирует оплату собственного подтверждённого бронирования.
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public void payMyBooking(@NotNull Long bookingId) {
        UserModel user = getCurrentUser();
        BookingModel booking = bookingRepository.findByIdAndUser(bookingId, user).orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        // Оплата разрешена только после подтверждения заявки сотрудником.
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ValidationException(
                "status",
                "Оплатить можно только подтверждённое бронирование"
            );
        }

        // Оплата должна быть выполнена до даты отправления.
        if (!booking.getDepartureDate().isAfter(LocalDate.now())) {
            throw new ValidationException(
                "departureDate",
                "Оплатить бронирование можно только до даты отправления"
            );
        }

        booking.setStatus(BookingStatus.PAID);

    }

    // Позволяет сотруднику завершить оплаченную поездку после окончания тура.
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional
    public void completeBooking(@NotNull Long bookingId) {
        BookingModel booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (booking.getStatus() != BookingStatus.PAID) {
            throw new ValidationException(
                "status",
                "Завершить можно только оплаченное бронирование"
            );
        }

        LocalDate endDate = booking.getDepartureDate().plusDays(booking.getTour().getDurationNights());

        // Разрешаем завершение в день окончания поездки или позднее.
        if (LocalDate.now().isBefore(endDate)) {
            throw new ValidationException(
                "departureDate",
                "Нельзя завершить поездку раньше даты её окончания"
            );
        }

        booking.setStatus(BookingStatus.COMPLETED);

    }

    private UserModel getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

}
