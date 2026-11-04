package ru.naboyshchikovfp.tour_agency.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ru.naboyshchikovfp.tour_agency.support.TestData.*;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.naboyshchikovfp.tour_agency.exceptions.*;
import ru.naboyshchikovfp.tour_agency.models.*;
import ru.naboyshchikovfp.tour_agency.repositories.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock ReviewRepository reviews;
    @Mock BookingRepository bookings;
    @Mock UserRepository users;
    @Mock TourRepository tours;
    @InjectMocks ReviewService service;
    UserModel user;

    @BeforeEach void login() {
        authenticate("client@example.com", "USER");
        user = user();
    }
    @AfterEach void logout() { SecurityContextHolder.clearContext(); }

    private BookingModel ownBooking(BookingStatus status) {
        var booking = new BookingModel(user, tour(), LocalDate.now().minusDays(10), 1, 0);
        booking.setStatus(status);
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(bookings.findByIdAndUser(1L, user)).thenReturn(Optional.of(booking));
        return booking;
    }

    @Test
    void completedTripCreatesReviewWithAuthorAndTourFromBooking() {
        var booking = ownBooking(BookingStatus.COMPLETED);
        when(reviews.save(any())).thenAnswer(call -> call.getArgument(0));
        service.create(1L, reviewDto());
        var captor = ArgumentCaptor.forClass(ReviewModel.class);
        verify(reviews).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getTour()).isSameAs(booking.getTour());
        assertThat(captor.getValue().getBooking()).isSameAs(booking);
        assertThat(captor.getValue().getRating()).isEqualTo(5);
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = "COMPLETED", mode = EnumSource.Mode.EXCLUDE)
    void reviewIsRejectedBeforeCompletion(BookingStatus status) {
        ownBooking(status);
        assertThatThrownBy(() -> service.create(1L, reviewDto())).isInstanceOf(ValidationException.class);
        verify(reviews, never()).save(any());
    }

    @Test
    void secondReviewForSameBookingIsRejected() {
        var booking = ownBooking(BookingStatus.COMPLETED);
        when(reviews.existsByBooking(booking)).thenReturn(true);
        assertThatThrownBy(() -> service.create(1L, reviewDto())).isInstanceOf(ValidationException.class);
        verify(reviews, never()).save(any());
    }

    @Test
    void anotherUsersBookingCannotBeReviewed() {
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service.create(1L, reviewDto())).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(reviews);
    }

    @Test
    void existingTourWithNoReviewsReturnsEmptyList() {
        var tour = tour();
        when(tours.findById(1L)).thenReturn(Optional.of(tour));
        assertThat(service.getByTour(1L)).isEmpty();
    }

    @Test
    void missingTourIsReported() {
        assertThatThrownBy(() -> service.getByTour(99L)).isInstanceOf(NotFoundException.class);
    }
}
