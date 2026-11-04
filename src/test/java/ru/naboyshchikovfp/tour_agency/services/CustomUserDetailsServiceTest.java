package ru.naboyshchikovfp.tour_agency.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.naboyshchikovfp.tour_agency.support.TestData.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.naboyshchikovfp.tour_agency.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @Mock UserRepository repository;
    @InjectMocks CustomUserDetailsService service;

    @Test
    void loginNormalizesEmailAndPropagatesHashRoleAndBlockedState() {
        var user = user();
        user.setEnabled(false);
        when(repository.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        var details = service.loadUserByUsername(" CLIENT@Example.com ");
        assertThat(details.getUsername()).isEqualTo("client@example.com");
        assertThat(details.getPassword()).isEqualTo("stored-hash");
        assertThat(details.isEnabled()).isFalse();
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    void unknownEmailIsRejected() {
        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
