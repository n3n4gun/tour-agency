package ru.naboyshchikovfp.tour_agency.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ru.naboyshchikovfp.tour_agency.support.TestData.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.naboyshchikovfp.tour_agency.exceptions.*;
import ru.naboyshchikovfp.tour_agency.models.HotelModel;
import ru.naboyshchikovfp.tour_agency.repositories.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {
    @Mock HotelRepository hotels;
    @Mock TourRepository tours;
    @InjectMocks HotelService service;

    @Test
    void creationTrimsTextBeforeSaving() {
        when(hotels.save(any())).thenAnswer(call -> call.getArgument(0));
        service.create(hotelDto());
        var captor = ArgumentCaptor.forClass(HotelModel.class);
        verify(hotels).save(captor.capture());
        assertThat(captor.getValue().getHotelName()).isEqualTo("Seaside");
        assertThat(captor.getValue().getCityName()).isEqualTo("Sochi");
        assertThat(captor.getValue().getAddress()).isEqualTo("Central street 10");
    }

    @Test
    void duplicateNamePreventsCreation() {
        when(hotels.existsByHotelName("Seaside")).thenReturn(true);
        assertThatThrownBy(() -> service.create(hotelDto())).isInstanceOf(ValidationException.class);
        verify(hotels, never()).save(any());
    }

    @Test
    void updateAllowsKeepingOwnNameAndTrimsAddress() {
        var hotel = hotel();
        when(hotels.findById(1L)).thenReturn(Optional.of(hotel));
        service.update(1L, hotelDto());
        verify(hotels).existsByHotelNameAndIdNot("Seaside", 1L);
        assertThat(hotel.getAddress()).isEqualTo("Central street 10");
    }

    @Test
    void duplicateNamePreventsUpdate() {
        var hotel = hotel();
        when(hotels.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotels.existsByHotelNameAndIdNot("Seaside", 1L)).thenReturn(true);
        assertThatThrownBy(() -> service.update(1L, hotelDto())).isInstanceOf(ValidationException.class);
    }

    @Test
    void hotelUsedByTourCannotBeDeleted() {
        var hotel = hotel();
        when(hotels.findById(1L)).thenReturn(Optional.of(hotel));
        when(tours.existsByHotelId(1L)).thenReturn(true);
        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(ValidationException.class);
        verify(hotels, never()).delete(any());
    }

    @Test
    void unusedHotelCanBeDeleted() {
        var hotel = hotel();
        when(hotels.findById(1L)).thenReturn(Optional.of(hotel));
        service.delete(1L);
        verify(hotels).delete(hotel);
    }

    @Test
    void missingHotelIsReported() {
        assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(NotFoundException.class);
    }
}
