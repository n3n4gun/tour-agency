package ru.naboyshchikovfp.tour_agency.services;

import java.util.Locale;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import ru.naboyshchikovfp.tour_agency.dto.UserProfileDto;
import ru.naboyshchikovfp.tour_agency.exceptions.NotFoundException;
import ru.naboyshchikovfp.tour_agency.dto.UserRegistrationDto;
import ru.naboyshchikovfp.tour_agency.dto.UserListDto;
import ru.naboyshchikovfp.tour_agency.exceptions.ValidationException;
import ru.naboyshchikovfp.tour_agency.models.UserModel;
import ru.naboyshchikovfp.tour_agency.repositories.UserRepository;

@Service
@Validated  
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional 
    public UserModel register(@NotNull @Valid UserRegistrationDto dto) {
        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        String phone;

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new ValidationException("confirmPassword", "Пароли не совпадают");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("email", "Пользователь с таким email уже зарегистрирован");
        }

        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            phone = null;
        } else {
            phone = dto.getPhone().trim();
        }

        UserModel user = new UserModel(email, passwordEncoder.encode(dto.getPassword()),
                    dto.getFirstName().trim(), dto.getLastName().trim(), phone, true);

        return userRepository.save(user);

    }

    // readOnly = true - транзакция только на чтение
    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long userId) {
        UserModel user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return UserProfileDto.fromModel(user);

    }

    // метод обновления пользовательских данных
    @Transactional 
    public void updateProfile(Long userId, @NotNull @Valid UserProfileDto dto) {
        UserModel user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());

        String phone = dto.getPhone();
        user.setPhone(phone == null || phone.isBlank() ? null : phone.trim());

    }

    // true - разблокировать пользователя, false - заблокировать пользователя
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional 
    public void setEnabled(Long userId, boolean enabled) {
        UserModel user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        user.setEnabled(enabled);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<UserListDto> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserListDto::fromModel);
    }

}
