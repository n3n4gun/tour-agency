package ru.naboyshchikovfp.tour_agency.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ru.naboyshchikovfp.tour_agency.support.TestData.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.naboyshchikovfp.tour_agency.dto.*;
import ru.naboyshchikovfp.tour_agency.exceptions.*;
import ru.naboyshchikovfp.tour_agency.models.*;
import ru.naboyshchikovfp.tour_agency.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository repository;
    @Mock PasswordEncoder encoder;
    @InjectMocks UserService service;

    @BeforeEach
    void loginAdministrator() { authenticate("admin@example.com", "ADMIN"); }

    @AfterEach
    void clearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test
    void administratorCannotBlockOwnAccountRegardlessOfEmailCase() {
        UserModel account = user();
        authenticate("CLIENT@EXAMPLE.COM", "ADMIN");
        when(repository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.setEnabled(1L, false))
                .isInstanceOfSatisfying(ValidationException.class,
                        error -> assertThat(error.getField()).isEqualTo("enabled"));
        assertThat(account.isEnabled()).isTrue();
    }

    @Test
    void missingAccountCannotBeBlocked() {
        assertThatThrownBy(() -> service.setEnabled(99L, false))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void registrationNormalizesDataAndStoresEncodedPassword() {
        when(encoder.encode("Password123!")).thenReturn("encoded-password");
        when(repository.save(any(UserModel.class))).thenAnswer(call -> call.getArgument(0));
        UserModel saved = service.register(registration());
        assertThat(saved.getEmail()).isEqualTo("client@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(saved.getFirstName()).isEqualTo("Ivan");
        assertThat(saved.getLastName()).isEqualTo("Petrov");
        assertThat(saved.getPhone()).isNull();
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(saved.isEnabled()).isTrue();
    }

    @Test
    void mismatchedPasswordsDoNotWriteToDatabase() {
        UserRegistrationDto dto = registration();
        dto.setConfirmPassword("AnotherPassword");
        assertThatThrownBy(() -> service.register(dto))
                .isInstanceOfSatisfying(ValidationException.class,
                        error -> assertThat(error.getField()).isEqualTo("confirmPassword"));
        verifyNoInteractions(repository, encoder);
    }

    @Test
    void duplicateNormalizedEmailIsRejected() {
        when(repository.existsByEmail("client@example.com")).thenReturn(true);
        assertThatThrownBy(() -> service.register(registration())).isInstanceOf(ValidationException.class);
        verify(repository, never()).save(any());
        verifyNoInteractions(encoder);
    }

    @Test
    void profileUpdatePreservesCredentialsAndRole() {
        UserModel user = user();
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        UserProfileDto dto = new UserProfileDto();
        dto.setFirstName(" Petr ");
        dto.setLastName(" Ivanov ");
        dto.setPhone("");
        service.updateProfile(1L, dto);
        assertThat(user.getFirstName()).isEqualTo("Petr");
        assertThat(user.getLastName()).isEqualTo("Ivanov");
        assertThat(user.getPhone()).isNull();
        assertThat(user.getEmail()).isEqualTo("client@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("stored-hash");
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void missingProfileIsReported() {
        assertThatThrownBy(() -> service.getProfile(99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void accountCanBeBlockedAndUnblocked() {
        UserModel user = user();
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        service.setEnabled(1L, false);
        assertThat(user.isEnabled()).isFalse();
        service.setEnabled(1L, true);
        assertThat(user.isEnabled()).isTrue();
    }
}
