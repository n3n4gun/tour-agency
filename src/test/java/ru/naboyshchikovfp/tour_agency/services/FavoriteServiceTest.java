package ru.naboyshchikovfp.tour_agency.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ru.naboyshchikovfp.tour_agency.support.TestData.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.naboyshchikovfp.tour_agency.exceptions.NotFoundException;
import ru.naboyshchikovfp.tour_agency.models.*;
import ru.naboyshchikovfp.tour_agency.repositories.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {
    @Mock FavoriteRepository favorites;
    @Mock TourRepository tours;
    @Mock UserRepository users;
    @InjectMocks FavoriteService service;
    UserModel user;
    TourModel tour;

    @BeforeEach void login() {
        authenticate("client@example.com", "USER");
        user = user();
        tour = tour();
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }
    @AfterEach void logout() { SecurityContextHolder.clearContext(); }

    @Test
    void newFavoriteBelongsToCurrentUser() {
        when(tours.findById(1L)).thenReturn(Optional.of(tour));
        service.add(1L);
        var captor = ArgumentCaptor.forClass(FavoriteModel.class);
        verify(favorites).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getTour()).isSameAs(tour);
    }

    @Test
    void repeatedAdditionDoesNotCreateDuplicate() {
        when(tours.findById(1L)).thenReturn(Optional.of(tour));
        when(favorites.existsByUserAndTour(user, tour)).thenReturn(true);
        service.add(1L);
        verify(favorites, never()).save(any());
    }

    @Test
    void missingTourCannotBeAdded() {
        assertThatThrownBy(() -> service.add(99L)).isInstanceOf(NotFoundException.class);
        verify(favorites, never()).save(any());
    }

    @Test
    void removalUsesCurrentUsersFavorite() {
        var favorite = new FavoriteModel(user, tour);
        when(tours.findById(1L)).thenReturn(Optional.of(tour));
        when(favorites.findByUserAndTour(user, tour)).thenReturn(Optional.of(favorite));
        service.remove(1L);
        verify(favorites).delete(favorite);
    }

    @Test
    void removalOfAbsentFavoriteDoesNothing() {
        when(tours.findById(1L)).thenReturn(Optional.of(tour));
        service.remove(1L);
        verify(favorites, never()).delete(any());
    }

    @Test
    void favoritesAreMappedToPublicTourData() {
        when(favorites.findByUser(user)).thenReturn(List.of(new FavoriteModel(user, tour)));
        assertThat(service.getMyFavorites()).singleElement().satisfies(dto -> {
            assertThat(dto.tourName()).isEqualTo(tour.getTourName());
            assertThat(dto.hotelName()).isEqualTo(tour.getHotel().getHotelName());
        });
    }
}
