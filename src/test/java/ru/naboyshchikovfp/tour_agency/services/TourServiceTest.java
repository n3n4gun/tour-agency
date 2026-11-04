package ru.naboyshchikovfp.tour_agency.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ru.naboyshchikovfp.tour_agency.support.TestData.*;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import ru.naboyshchikovfp.tour_agency.dto.TourFilterDto;
import ru.naboyshchikovfp.tour_agency.exceptions.*;
import ru.naboyshchikovfp.tour_agency.repositories.*;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {
    @Mock TourRepository tours;
    @Mock HotelRepository hotels;
    @Mock BookingRepository bookings;
    @Mock FavoriteRepository favorites;
    @InjectMocks TourService service;

    static Stream<Consumer<TourFilterDto>> invalidFilters() {
        return Stream.of(
                f -> f.setMinPrice(new BigDecimal("-1")),
                f -> f.setMaxPrice(new BigDecimal("-1")),
                f -> { f.setMinPrice(BigDecimal.TEN); f.setMaxPrice(BigDecimal.ONE); },
                f -> f.setMinNights(0),
                f -> f.setMaxNights(0),
                f -> { f.setMinNights(10); f.setMaxNights(2); });
    }

    @ParameterizedTest
    @MethodSource("invalidFilters")
    void invalidRangesAreRejectedBeforeQueryingDatabase(Consumer<TourFilterDto> configure) {
        TourFilterDto filter = new TourFilterDto();
        configure.accept(filter);
        assertThatThrownBy(() -> service.search(filter, PageRequest.of(0, 10)))
                .isInstanceOf(ValidationException.class);
        verifyNoInteractions(tours);
    }

    @Test
    void creationRequiresExistingHotel() {
        assertThatThrownBy(() -> service.create(tourDto(99L))).isInstanceOf(NotFoundException.class);
        verify(tours, never()).save(any());
    }

    @Test
    void bookedTourCannotChangeDuration() {
        var tour = tour();
        ReflectionTestUtils.setField(tour.getHotel(), "id", 1L);
        when(tours.findById(1L)).thenReturn(Optional.of(tour));
        when(hotels.findById(1L)).thenReturn(Optional.of(tour.getHotel()));
        when(bookings.existsByTourId(1L)).thenReturn(true);
        var dto = tourDto(1L);
        dto.setDurationNights(10);
        assertThatThrownBy(() -> service.update(1L, dto)).isInstanceOf(ValidationException.class);
        assertThat(tour.getDurationNights()).isEqualTo(5);
    }

    @Test
    void bookedTourCannotChangeHotel() {
        var tour = tour();
        ReflectionTestUtils.setField(tour.getHotel(), "id", 1L);
        var anotherHotel = hotel();
        ReflectionTestUtils.setField(anotherHotel, "id", 2L);
        when(tours.findById(1L)).thenReturn(Optional.of(tour));
        when(hotels.findById(2L)).thenReturn(Optional.of(anotherHotel));
        when(bookings.existsByTourId(1L)).thenReturn(true);
        assertThatThrownBy(() -> service.update(1L, tourDto(2L)))
                .isInstanceOf(ValidationException.class);
        assertThat(tour.getHotel().getId()).isEqualTo(1L);
    }

    @Test
    void bookedTourCanChangePriceWithoutChangingConditions() {
        var tour = tour();
        ReflectionTestUtils.setField(tour.getHotel(), "id", 1L);
        when(tours.findById(1L)).thenReturn(Optional.of(tour));
        when(hotels.findById(1L)).thenReturn(Optional.of(tour.getHotel()));
        var dto = tourDto(1L);
        dto.setPricePerAdultPerson(new BigDecimal("15000.00"));
        service.update(1L, dto);
        assertThat(tour.getPricePerAdultPerson()).isEqualByComparingTo("15000");
    }

    @Test
    void bookingsPreventTourDeletion() {
        when(tours.findById(1L)).thenReturn(Optional.of(tour()));
        when(bookings.existsByTourId(1L)).thenReturn(true);
        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(ValidationException.class);
        verify(tours, never()).delete(any(ru.naboyshchikovfp.tour_agency.models.TourModel.class));
    }

    @Test
    void favoritesPreventTourDeletion() {
        when(tours.findById(1L)).thenReturn(Optional.of(tour()));
        when(favorites.existsByTourId(1L)).thenReturn(true);
        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(ValidationException.class);
        verify(tours, never()).delete(any(ru.naboyshchikovfp.tour_agency.models.TourModel.class));
    }

    @Test
    void unusedTourCanBeDeleted() {
        var tour = tour();
        when(tours.findById(1L)).thenReturn(Optional.of(tour));
        service.delete(1L);
        verify(tours).delete(tour);
    }
}
