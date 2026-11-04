package ru.naboyshchikovfp.tour_agency.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ru.naboyshchikovfp.tour_agency.support.TestData.*;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.naboyshchikovfp.tour_agency.exceptions.*;
import ru.naboyshchikovfp.tour_agency.models.*;
import ru.naboyshchikovfp.tour_agency.repositories.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock BookingRepository bookings;
    @Mock TourRepository tours;
    @Mock UserRepository users;
    @InjectMocks BookingService service;

    @BeforeEach void login() { authenticate("client@example.com", "USER"); }
    @AfterEach void logout() { SecurityContextHolder.clearContext(); }

    private void ownBooking(BookingModel booking) {
        when(users.findByEmail("client@example.com")).thenReturn(Optional.of(booking.getUser()));
        when(bookings.findByIdAndUser(1L, booking.getUser())).thenReturn(Optional.of(booking));
    }

    @Test
    void creationCalculatesPriceAndUsesCurrentUser() {
        UserModel user = user();
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tours.findById(1L)).thenReturn(Optional.of(tour()));
        when(bookings.save(any())).thenAnswer(call -> call.getArgument(0));
        service.create(1L, bookingDto());
        ArgumentCaptor<BookingModel> captor = ArgumentCaptor.forClass(BookingModel.class);
        verify(bookings).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getTotalPrice()).isEqualByComparingTo("24000.00");
        assertThat(captor.getValue().getStatus()).isEqualTo(BookingStatus.NEW);
    }

    @Test
    void anotherUsersBookingCannotBeReadOrChanged() {
        when(users.findByEmail("client@example.com")).thenReturn(Optional.of(user()));
        assertThatThrownBy(() -> service.getMyBooking(1L)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.cancelMyBooking(1L)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.payMyBooking(1L)).isInstanceOf(NotFoundException.class);
        verify(bookings, never()).findById(anyLong());
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"NEW", "CONFIRMED"})
    void cancellationAcceptsOnlyUnpaidFutureTrips(BookingStatus status) {
        var booking = booking(status, LocalDate.now().plusDays(10));
        ownBooking(booking);
        service.cancelMyBooking(1L);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"PAID", "COMPLETED", "CANCELLED"})
    void cancellationRejectsOtherStatuses(BookingStatus status) {
        var booking = booking(status, LocalDate.now().plusDays(10));
        ownBooking(booking);
        assertThatThrownBy(() -> service.cancelMyBooking(1L)).isInstanceOf(ValidationException.class);
        assertThat(booking.getStatus()).isEqualTo(status);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void cancellationRejectsDepartureTodayAndInPast(int days) {
        var booking = booking(BookingStatus.NEW, LocalDate.now().plusDays(days));
        ownBooking(booking);
        assertThatThrownBy(() -> service.cancelMyBooking(1L)).isInstanceOf(ValidationException.class);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.NEW);
    }

    @Test
    void confirmationAcceptsFutureNewBooking() {
        var booking = booking(BookingStatus.NEW, LocalDate.now().plusDays(10));
        when(bookings.findById(1L)).thenReturn(Optional.of(booking));
        service.confirmBooking(1L);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = "NEW", mode = EnumSource.Mode.EXCLUDE)
    void confirmationRejectsNonNewBookings(BookingStatus status) {
        var booking = booking(status, LocalDate.now().plusDays(10));
        when(bookings.findById(1L)).thenReturn(Optional.of(booking));
        assertThatThrownBy(() -> service.confirmBooking(1L)).isInstanceOf(ValidationException.class);
        assertThat(booking.getStatus()).isEqualTo(status);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void confirmationRejectsDepartureTodayAndInPast(int days) {
        var booking = booking(BookingStatus.NEW, LocalDate.now().plusDays(days));
        when(bookings.findById(1L)).thenReturn(Optional.of(booking));
        assertThatThrownBy(() -> service.confirmBooking(1L)).isInstanceOf(ValidationException.class);
    }

    @Test
    void paymentAcceptsConfirmedFutureBooking() {
        var booking = booking(BookingStatus.CONFIRMED, LocalDate.now().plusDays(10));
        ownBooking(booking);
        service.payMyBooking(1L);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PAID);
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = "CONFIRMED", mode = EnumSource.Mode.EXCLUDE)
    void paymentRejectsOtherStatuses(BookingStatus status) {
        var booking = booking(status, LocalDate.now().plusDays(10));
        ownBooking(booking);
        assertThatThrownBy(() -> service.payMyBooking(1L)).isInstanceOf(ValidationException.class);
        assertThat(booking.getStatus()).isEqualTo(status);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void paymentRejectsDepartureTodayAndInPast(int days) {
        ownBooking(booking(BookingStatus.CONFIRMED, LocalDate.now().plusDays(days)));
        assertThatThrownBy(() -> service.payMyBooking(1L)).isInstanceOf(ValidationException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-6, -5})
    void completionAcceptsEndDateTodayAndInPast(int departureOffset) {
        var booking = booking(BookingStatus.PAID, LocalDate.now().plusDays(departureOffset));
        when(bookings.findById(1L)).thenReturn(Optional.of(booking));
        service.completeBooking(1L);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    void completionRejectsTripThatHasNotEnded() {
        var booking = booking(BookingStatus.PAID, LocalDate.now().minusDays(4));
        when(bookings.findById(1L)).thenReturn(Optional.of(booking));
        assertThatThrownBy(() -> service.completeBooking(1L)).isInstanceOf(ValidationException.class);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PAID);
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = "PAID", mode = EnumSource.Mode.EXCLUDE)
    void completionRejectsUnpaidOrTerminalBookings(BookingStatus status) {
        var booking = booking(status, LocalDate.now().minusDays(10));
        when(bookings.findById(1L)).thenReturn(Optional.of(booking));
        assertThatThrownBy(() -> service.completeBooking(1L)).isInstanceOf(ValidationException.class);
    }
}
