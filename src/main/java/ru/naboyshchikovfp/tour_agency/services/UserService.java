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
import org.springframework.security.core.context.SecurityContextHolder;

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

    // Передаёт зависимости сервиса через конструктор.
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Проверяет регистрационные данные, хеширует пароль и сохраняет пользователя.
    @Transactional 
    public UserModel register(@NotNull @Valid UserRegistrationDto dto) {
        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        String phone;


        // Отклоняем регистрацию, если пароль и его подтверждение не совпадают.
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new ValidationException("confirmPassword", "Пароли не совпадают");
        }
        // Проверяем, не занят ли нормализованный email.
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("email", "Пользователь с таким email уже зарегистрирован");
        }


        // Если телефон не заполнен, сохраняем null; иначе убираем пробелы по краям.
        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            phone = null;
        } else {
            phone = dto.getPhone().trim();
        }

        UserModel user = new UserModel(email, passwordEncoder.encode(dto.getPassword()),
                    dto.getFirstName().trim(), dto.getLastName().trim(), phone, true);

        return userRepository.save(user);

    }

    // Возвращает профиль пользователя без пароля и роли.
    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long userId) {
        UserModel user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return UserProfileDto.fromModel(user);

    }

    // Обновляет профиль; JPA сохраняет изменения при завершении транзакции.
    @Transactional 
    public void updateProfile(Long userId, @NotNull @Valid UserProfileDto dto) {
        UserModel user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());

        String phone = dto.getPhone();
        // Пустой телефон заменяем на null, у заполненного убираем пробелы по краям.
        user.setPhone(phone == null || phone.isBlank() ? null : phone.trim());

    }

    // Позволяет администратору блокировать (false) и разблокировать (true) пользователя.
    // Изменяет доступ пользователя и запрещает блокировку своей учётной записи.
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional 
    public void setEnabled(Long userId, boolean enabled) {
        UserModel user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!enabled && user.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new ValidationException(
                "enabled",
                "Нельзя заблокировать собственную учётную запись"
            );
        }

        user.setEnabled(enabled);
    }

    // Возвращает администратору страницу пользователей без хешей паролей.
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<UserListDto> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserListDto::fromModel);
    }

    // Возвращает профиль пользователя, вошедшего в приложение
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public UserProfileDto getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserModel user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return UserProfileDto.fromModel(user);

    }

    // Обновляет имя, фамилию и телефон текущего пользователя.
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void updateMyProfile(@NotNull @Valid UserProfileDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserModel user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());

        String phone = dto.getPhone();
        user.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
    }

}
