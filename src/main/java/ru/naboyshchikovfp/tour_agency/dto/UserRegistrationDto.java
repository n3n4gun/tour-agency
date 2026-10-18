package ru.naboyshchikovfp.tour_agency.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRegistrationDto {
    @NotBlank(message = "Укажите email")
    @Email(message = "Некорректный email")
    @Size(max = 100, message = "Email не должен превышать 100 символов")
    private String email;

    @NotBlank(message = "Укажите пароль")
    @Size(min = 8, max = 72, message = "Пароль должен быть от 8 до 72 символов")
    private String password;

    // в базу не сохраняется, нужен только для сверки с password в сервисе
    @NotBlank(message = "Повторите пароль")
    private String confirmPassword;

    @NotBlank(message = "Укажите имя")
    @Size(max = 50, message = "Имя не должно превышать 50 символов")
    private String firstName;

    @NotBlank(message = "Укажите фамилию")
    @Size(max = 50, message = "Фамилия не должна превышать 50 символов")
    private String lastName;

    @Pattern(regexp = "^$|^\\+?[0-9]{10,15}$", message = "Некорректный номер телефона")
    private String phone;

    public UserRegistrationDto() {}

    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmail() { return this.email; }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() { return this.password; }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    public String getConfirmPassword() { return this.confirmPassword; }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getFirstName() { return this.firstName; }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getLastName() { return this.lastName; }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getPhone() { return this.phone; }

}
