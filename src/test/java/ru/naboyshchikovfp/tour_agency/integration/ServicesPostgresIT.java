package ru.naboyshchikovfp.tour_agency.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.naboyshchikovfp.tour_agency.support.TestData.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import jakarta.validation.ConstraintViolationException;
import ru.naboyshchikovfp.tour_agency.dto.*;
import ru.naboyshchikovfp.tour_agency.exceptions.*;
import ru.naboyshchikovfp.tour_agency.models.*;
import ru.naboyshchikovfp.tour_agency.repositories.*;
import ru.naboyshchikovfp.tour_agency.services.*;

/**
 * Реальные Spring-прокси, транзакции и PostgreSQL. Внешней тестовой транзакции нет:
 * после вызова сервиса читаем запись заново, проверяя фактическое сохранение.
 */
@SpringBootTest
@ActiveProfiles("postgres-test")
@Testcontainers
class ServicesPostgresIT {
    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("tour_agency_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired UserService userService;
    @Autowired CustomUserDetailsService userDetailsService;
    @Autowired HotelService hotelService;
    @Autowired TourService tourService;
    @Autowired BookingService bookingService;
    @Autowired FavoriteService favoriteService;
    @Autowired ReviewService reviewService;
    @Autowired UserRepository users;
    @Autowired HotelRepository hotels;
    @Autowired TourRepository tours;
    @Autowired BookingRepository bookings;
    @Autowired FavoriteRepository favorites;
    @Autowired ReviewRepository reviews;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired WebApplicationContext webContext;

    UserModel client;
    UserModel otherClient;
    HotelModel hotel;
    TourModel tour;

    @BeforeEach
    void prepareDatabase() {
        SecurityContextHolder.clearContext();
        // URL получен от контейнера: очистка затрагивает исключительно временную БД.
        jdbc.execute("TRUNCATE TABLE reviews, favorites, bookings, tours, hotels, users RESTART IDENTITY CASCADE");
        client = users.save(ru.naboyshchikovfp.tour_agency.support.TestData.user());
        otherClient = users.save(new UserModel("other@example.com", "stored-hash",
                "Petr", "Ivanov", null, true));
        hotel = hotels.save(hotel());
        tour = tours.save(new TourModel("Sea holiday", "Description", "Beach", hotel,
                new BigDecimal("10000.00"), new BigDecimal("4000.00"), 5));
        loginClient();
    }

    @AfterEach
    void clearAuthentication() { SecurityContextHolder.clearContext(); }

    private void loginClient() { authenticate(client.getEmail(), "USER"); }
    private void loginManager() { authenticate("manager@example.com", "MANAGER"); }
    private void loginAdmin() { authenticate("admin@example.com", "ADMIN"); }

    private MockMvc web() {
        return MockMvcBuilders.webAppContextSetup(webContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void adminUsersPageRendersAccountsAndAccessFormsWithoutPasswordHashes() throws Exception {
        web().perform(get("/admin/users").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("userPage"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(client.getEmail())))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/admin/users/" + client.getId() + "/enabled")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("stored-hash"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "MANAGER"})
    void nonAdministratorsCannotListOrBlockUsers(String role) throws Exception {
        MockMvc mvc = web();
        mvc.perform(get("/admin/users").with(user(client.getEmail()).roles(role)))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));
        mvc.perform(post("/admin/users/{id}/enabled", otherClient.getId())
                        .with(user(client.getEmail()).roles(role)).with(csrf()).param("enabled", "false"))
                .andExpect(status().isForbidden());
        assertThat(users.findById(otherClient.getId()).orElseThrow().isEnabled()).isTrue();
    }

    @Test
    void anonymousUserIsRedirectedToLoginFromUserManagement() throws Exception {
        SecurityContextHolder.clearContext();
        web().perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void webBlockingAndUnblockingArePersisted() throws Exception {
        MockMvc mvc = web();
        for (boolean enabled : new boolean[]{false, true}) {
            mvc.perform(post("/admin/users/{id}/enabled", client.getId())
                            .with(user("admin@example.com").roles("ADMIN")).with(csrf())
                            .param("enabled", Boolean.toString(enabled)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/users"))
                    .andExpect(flash().attributeExists("successMessage"));
            assertThat(users.findById(client.getId()).orElseThrow().isEnabled()).isEqualTo(enabled);
        }
    }

    @Test
    void adminCannotBlockOwnAccountThroughWeb() throws Exception {
        client.setRole(UserRole.ADMIN);
        users.save(client);
        web().perform(post("/admin/users/{id}/enabled", client.getId())
                        .with(user(client.getEmail()).roles("ADMIN")).with(csrf()).param("enabled", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attributeCount(1));
        assertThat(users.findById(client.getId()).orElseThrow().isEnabled()).isTrue();
    }

    @Test
    void blockingRequiresCsrfAndMissingAccountReturns404() throws Exception {
        MockMvc mvc = web();
        mvc.perform(post("/admin/users/{id}/enabled", client.getId())
                        .with(user("admin@example.com").roles("ADMIN")).param("enabled", "false"))
                .andExpect(status().isForbidden());
        assertThat(users.findById(client.getId()).orElseThrow().isEnabled()).isTrue();
        mvc.perform(post("/admin/users/999999/enabled")
                        .with(user("admin@example.com").roles("ADMIN")).with(csrf()).param("enabled", "false"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void blockedAccountCannotLogInUntilUnblocked() throws Exception {
        client.setPasswordHash(passwordEncoder.encode("Password123!"));
        users.save(client);
        loginAdmin();
        userService.setEnabled(client.getId(), false);
        SecurityContextHolder.clearContext();
        MockMvc mvc = web();
        mvc.perform(post("/login").with(csrf())
                        .param("username", client.getEmail()).param("password", "Password123!"))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated());
        loginAdmin();
        userService.setEnabled(client.getId(), true);
        SecurityContextHolder.clearContext();
        mvc.perform(post("/login").with(csrf())
                        .param("username", client.getEmail()).param("password", "Password123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated()
                        .withUsername(client.getEmail()));
    }

    @Test
    void accessDeniedPageRendersWithForbiddenStatus() throws Exception {
        web().perform(get("/access-denied").with(user(client.getEmail()).roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Доступ запрещён")));
    }

    private BookingModel storedBooking(UserModel owner, BookingStatus status, LocalDate departure) {
        BookingModel booking = new BookingModel(owner, tour, departure, 2, 1);
        booking.setStatus(status);
        return bookings.save(booking);
    }

    @Test
    void registrationPersistsNormalizedEmailAndBcryptHash() {
        var dto = registration();
        dto.setEmail("NEW@Example.com");
        Long id = userService.register(dto).getId();
        UserModel saved = users.findById(id).orElseThrow();
        assertThat(saved.getEmail()).isEqualTo("new@example.com");
        assertThat(passwordEncoder.matches(dto.getPassword(), saved.getPasswordHash())).isTrue();
        assertThat(saved.getPasswordHash()).isNotEqualTo(dto.getPassword());
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void duplicateRegistrationDoesNotInsertAnotherUser() {
        long before = users.count();
        assertThatThrownBy(() -> userService.register(registration())).isInstanceOf(ValidationException.class);
        assertThat(users.count()).isEqualTo(before);
    }

    @Test
    void profileUpdateIsCommittedWithoutExplicitSave() {
        var dto = new UserProfileDto();
        dto.setFirstName(" Updated ");
        dto.setLastName(" Name ");
        dto.setPhone("");
        userService.updateProfile(client.getId(), dto);
        UserModel saved = users.findById(client.getId()).orElseThrow();
        assertThat(saved.getFirstName()).isEqualTo("Updated");
        assertThat(saved.getPhone()).isNull();
        assertThat(saved.getPasswordHash()).isEqualTo("stored-hash");
    }

    @Test
    void blockingIsCommittedAndVisibleToUserDetailsService() {
        loginAdmin();
        userService.setEnabled(client.getId(), false);
        assertThat(users.findById(client.getId()).orElseThrow().isEnabled()).isFalse();
        assertThat(userDetailsService.loadUserByUsername(" CLIENT@Example.com ").isEnabled()).isFalse();
        userService.setEnabled(client.getId(), true);
        assertThat(userDetailsService.loadUserByUsername(client.getEmail()).isEnabled()).isTrue();
    }

    @Test
    void userPaginationIncludesTotalCount() {
        loginAdmin();
        var page = userService.getUsers(PageRequest.of(0, 1, Sort.by("id")));
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void hotelCrudAndDuplicateNameValidationUseDatabase() {
        loginManager();
        var dto = hotelDto();
        dto.setHotelName(" New hotel ");
        Long id = hotelService.create(dto);
        assertThat(hotelService.getById(id).getHotelName()).isEqualTo("New hotel");
        assertThatThrownBy(() -> hotelService.create(dto)).isInstanceOf(ValidationException.class);
        dto.setAddress(" Updated address ");
        hotelService.update(id, dto);
        assertThat(hotels.findById(id).orElseThrow().getAddress()).isEqualTo("Updated address");
        hotelService.delete(id);
        assertThat(hotels.existsById(id)).isFalse();
    }

    @Test
    void hotelReferencedByTourCannotBeDeletedByServiceOrDatabase() {
        loginManager();
        assertThatThrownBy(() -> hotelService.delete(hotel.getId())).isInstanceOf(ValidationException.class);
        // Проверяем и сам внешний ключ, обходя проверку сервиса.
        assertThatThrownBy(() -> hotels.deleteById(hotel.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(hotels.existsById(hotel.getId())).isTrue();
    }

    @Test
    void tourCrudPersistsUpdatesAndLoadsHotelOutsideTransaction() {
        loginManager();
        Long id = tourService.create(tourDto(hotel.getId()));
        var dto = tourDto(hotel.getId());
        dto.setPricePerAdultPerson(new BigDecimal("17000.00"));
        tourService.update(id, dto);
        assertThat(tourService.getDetails(id).pricePerAdultPerson()).isEqualByComparingTo("17000");
        assertThat(tourService.getDetails(id).hotelName()).isEqualTo(hotel.getHotelName());
        tourService.delete(id);
        assertThat(tours.existsById(id)).isFalse();
    }

    @Test
    void combinedFiltersAndInclusiveBoundariesAreAppliedByPostgres() {
        var secondHotel = hotels.save(new HotelModel("Moscow", "Capital", 5, "Street 20", null));
        tours.save(new TourModel("City tour", null, "Excursion", secondHotel,
                new BigDecimal("20000"), BigDecimal.ZERO, 3));
        var filter = new TourFilterDto();
        filter.setCityName(" SOCHI ");
        filter.setTourTypeName(" beach ");
        filter.setMinPrice(new BigDecimal("10000"));
        filter.setMaxPrice(new BigDecimal("10000"));
        filter.setMinNights(5);
        filter.setMaxNights(5);
        // Публичное чтение работает без авторизации и с отключённым Open Session in View.
        SecurityContextHolder.clearContext();
        var page = tourService.search(filter, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).singleElement().satisfies(found -> {
            assertThat(found.id()).isEqualTo(tour.getId());
            assertThat(found.hotelName()).isEqualTo("Seaside");
        });
        filter.setCityName("Unknown city");
        assertThat(tourService.search(filter, PageRequest.of(0, 10))).isEmpty();
    }

    @Test
    void emptyFilterSupportsPaginationAndSorting() {
        tours.save(new TourModel("Another tour", null, "Beach", hotel,
                new BigDecimal("15000"), BigDecimal.ZERO, 7));
        var page = tourService.search(new TourFilterDto(),
                PageRequest.of(0, 1, Sort.by("pricePerAdultPerson").descending()));
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).singleElement()
                .satisfies(dto -> assertThat(dto.pricePerAdultPerson()).isEqualByComparingTo("15000"));
    }

    @Test
    void bookedTourKeepsConditionsAndBookingPriceSnapshot() {
        Long id = bookingService.create(tour.getId(), bookingDto());
        loginManager();
        var dto = tourDto(hotel.getId());
        dto.setDurationNights(8);
        assertThatThrownBy(() -> tourService.update(tour.getId(), dto)).isInstanceOf(ValidationException.class);
        assertThat(tours.findById(tour.getId()).orElseThrow().getDurationNights()).isEqualTo(5);
        dto.setDurationNights(5);
        dto.setPricePerAdultPerson(new BigDecimal("30000"));
        tourService.update(tour.getId(), dto);
        assertThat(bookings.findById(id).orElseThrow().getTotalPrice()).isEqualByComparingTo("24000");
        assertThatThrownBy(() -> tourService.delete(tour.getId())).isInstanceOf(ValidationException.class);
    }

    @Test
    void bookingCreationAndStatusChangesAreCommitted() {
        Long id = bookingService.create(tour.getId(), bookingDto());
        var created = bookings.findById(id).orElseThrow();
        assertThat(created.getTotalPrice()).isEqualByComparingTo("24000");
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.NEW);
        loginManager();
        bookingService.confirmBooking(id);
        assertThat(bookings.findById(id).orElseThrow().getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        loginClient();
        bookingService.payMyBooking(id);
        assertThat(bookings.findById(id).orElseThrow().getStatus()).isEqualTo(BookingStatus.PAID);
    }

    @Test
    void clientCanPayThroughWebAfterAdminConfirmation() throws Exception {
        var booking = storedBooking(client, BookingStatus.NEW, LocalDate.now().plusDays(10));
        Long id = booking.getId();
        MockMvc mvc = web();

        mvc.perform(post("/admin/bookings/{id}/confirm", id)
                        .with(user("admin@example.com").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/bookings"));
        assertThat(bookings.findById(id).orElseThrow().getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        // Проверяем реальный рендеринг Thymeleaf и наличие кнопки оплаты.
        mvc.perform(get("/bookings").with(user(client.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/bookings/" + id + "/pay")));

        mvc.perform(post("/bookings/{id}/pay", id)
                        .with(user(client.getEmail()).roles("USER")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attributeExists("successMessage"));
        assertThat(bookings.findById(id).orElseThrow().getStatus()).isEqualTo(BookingStatus.PAID);

        mvc.perform(get("/bookings").with(user(client.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("/bookings/" + id + "/pay"))));
    }

    @Test
    void webPaymentShowsBusinessErrorWithoutChangingStatus() throws Exception {
        var booking = storedBooking(client, BookingStatus.NEW, LocalDate.now().plusDays(10));
        web().perform(post("/bookings/{id}/pay", booking.getId())
                        .with(user(client.getEmail()).roles("USER")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attributeCount(1));
        assertThat(bookings.findById(booking.getId()).orElseThrow().getStatus()).isEqualTo(BookingStatus.NEW);
    }

    @Test
    void webPaymentCannotAccessAnotherClientsBooking() throws Exception {
        var booking = storedBooking(otherClient, BookingStatus.CONFIRMED, LocalDate.now().plusDays(10));
        web().perform(post("/bookings/{id}/pay", booking.getId())
                        .with(user(client.getEmail()).roles("USER")).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
        assertThat(bookings.findById(booking.getId()).orElseThrow().getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void webPaymentRequiresCsrfAndClientRole() throws Exception {
        var booking = storedBooking(client, BookingStatus.CONFIRMED, LocalDate.now().plusDays(10));
        MockMvc mvc = web();
        mvc.perform(post("/bookings/{id}/pay", booking.getId())
                        .with(user(client.getEmail()).roles("USER")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/bookings/{id}/pay", booking.getId())
                        .with(user("admin@example.com").roles("ADMIN")).with(csrf()))
                .andExpect(status().isForbidden());
        assertThat(bookings.findById(booking.getId()).orElseThrow().getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void ownBookingCancellationIsCommittedAndKeepsHistory() {
        Long id = bookingService.create(tour.getId(), bookingDto());
        bookingService.cancelMyBooking(id);
        assertThat(bookings.findById(id).orElseThrow().getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(bookingService.getMyBookings()).hasSize(1);
    }

    @Test
    void anotherUsersBookingIsInvisibleAndCannotBeCancelledPaidOrReviewed() {
        var another = storedBooking(otherClient, BookingStatus.COMPLETED, LocalDate.now().minusDays(10));
        assertThat(bookingService.getMyBookings()).isEmpty();
        assertThatThrownBy(() -> bookingService.getMyBooking(another.getId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> bookingService.cancelMyBooking(another.getId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> bookingService.payMyBooking(another.getId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> reviewService.create(another.getId(), reviewDto())).isInstanceOf(NotFoundException.class);
        assertThat(reviews.count()).isZero();
    }

    @Test
    void completedTripCanBeReviewedExactlyOnceAndReadAnonymously() {
        var booking = storedBooking(client, BookingStatus.PAID, LocalDate.now().minusDays(10));
        loginManager();
        bookingService.completeBooking(booking.getId());
        assertThat(bookings.findById(booking.getId()).orElseThrow().getStatus()).isEqualTo(BookingStatus.COMPLETED);
        loginClient();
        Long id = reviewService.create(booking.getId(), reviewDto());
        assertThat(reviews.findById(id).orElseThrow().getCreatedAt()).isNotNull();
        assertThatThrownBy(() -> reviewService.create(booking.getId(), reviewDto())).isInstanceOf(ValidationException.class);
        SecurityContextHolder.clearContext();
        assertThat(reviewService.getByTour(tour.getId())).singleElement().satisfies(dto -> {
            assertThat(dto.authorName()).isEqualTo("Ivan");
            assertThat(dto.rating()).isEqualTo(5);
        });
    }

    @Test
    void failedStatusTransitionDoesNotChangePersistedBooking() {
        var booking = storedBooking(client, BookingStatus.PAID, LocalDate.now().plusDays(10));
        assertThatThrownBy(() -> bookingService.cancelMyBooking(booking.getId())).isInstanceOf(ValidationException.class);
        loginManager();
        assertThatThrownBy(() -> bookingService.completeBooking(booking.getId())).isInstanceOf(ValidationException.class);
        assertThat(bookings.findById(booking.getId()).orElseThrow().getStatus()).isEqualTo(BookingStatus.PAID);
    }

    @Test
    void favoritesAreIdempotentIsolatedAndLoadHotelData() {
        favoriteService.add(tour.getId());
        favoriteService.add(tour.getId());
        assertThat(favorites.count()).isEqualTo(1);
        assertThat(favoriteService.getMyFavorites()).singleElement()
                .satisfies(dto -> assertThat(dto.hotelName()).isEqualTo("Seaside"));
        authenticate(otherClient.getEmail(), "USER");
        assertThat(favoriteService.getMyFavorites()).isEmpty();
        favoriteService.remove(tour.getId());
        assertThat(favorites.count()).isEqualTo(1);
        loginClient();
        favoriteService.remove(tour.getId());
        favoriteService.remove(tour.getId());
        assertThat(favorites.count()).isZero();
    }

    @Test
    void favoritedTourCannotBeDeleted() {
        favoriteService.add(tour.getId());
        loginManager();
        assertThatThrownBy(() -> tourService.delete(tour.getId())).isInstanceOf(ValidationException.class);
        assertThat(tours.existsById(tour.getId())).isTrue();
    }

    @Test
    void postgresEnforcesUniqueEmail() {
        assertThatThrownBy(() -> users.saveAndFlush(ru.naboyshchikovfp.tour_agency.support.TestData.user())).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(users.count()).isEqualTo(2);
    }

    @Test
    void postgresEnforcesUniqueUserTourFavorite() {
        favorites.saveAndFlush(new FavoriteModel(client, tour));
        assertThatThrownBy(() -> favorites.saveAndFlush(new FavoriteModel(client, tour)))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(favorites.count()).isEqualTo(1);
    }

    @Test
    void postgresEnforcesOneReviewPerBooking() {
        var booking = storedBooking(client, BookingStatus.COMPLETED, LocalDate.now().minusDays(10));
        reviews.saveAndFlush(new ReviewModel(booking, 5, "First"));
        assertThatThrownBy(() -> reviews.saveAndFlush(new ReviewModel(booking, 4, "Second")))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(reviews.count()).isEqualTo(1);
    }

    @Test
    void serviceProxiesEnforceRoles() {
        assertThatThrownBy(() -> hotelService.create(hotelDto())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> tourService.create(tourDto(hotel.getId()))).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> userService.setEnabled(client.getId(), false)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> userService.getUsers(PageRequest.of(0, 10))).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> bookingService.confirmBooking(1L)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> bookingService.getAllBookings()).isInstanceOf(AccessDeniedException.class);
        loginManager();
        assertThatThrownBy(() -> userService.setEnabled(client.getId(), false)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> bookingService.create(tour.getId(), bookingDto())).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void anonymousUserCannotCreatePrivateRecords() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> bookingService.create(tour.getId(), bookingDto()))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
        assertThatThrownBy(() -> favoriteService.add(tour.getId()))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
        assertThatThrownBy(() -> reviewService.create(1L, reviewDto()))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void beanValidationRejectsInvalidAndNullDtoBeforeDatabaseWrite() {
        var dto = bookingDto();
        dto.setDepartureDate(LocalDate.now().minusDays(1));
        dto.setAdultsCount(0);
        assertThatThrownBy(() -> bookingService.create(tour.getId(), dto)).isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> bookingService.create(tour.getId(), null)).isInstanceOf(ConstraintViolationException.class);
        assertThat(bookings.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6})
    void invalidReviewRatingIsRejectedBySpringValidation(int rating) {
        var booking = storedBooking(client, BookingStatus.COMPLETED, LocalDate.now().minusDays(10));
        var dto = reviewDto();
        dto.setRating(rating);
        assertThatThrownBy(() -> reviewService.create(booking.getId(), dto)).isInstanceOf(ConstraintViolationException.class);
        assertThat(reviews.count()).isZero();
    }
}
