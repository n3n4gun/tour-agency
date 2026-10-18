package ru.naboyshchikovfp.tour_agency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.naboyshchikovfp.tour_agency.models.UserModel;

// email, пароль и роль через профиль не меняются, поэтому их здесь нет
public class UserProfileDto {
    @NotBlank(message = "Укажите имя")
    @Size(max = 50, message = "Имя не должно превышать 50 символов")
    private String firstName;

    @NotBlank(message = "Укажите фамилию")
    @Size(max = 50, message = "Фамилия не должна превышать 50 символов")
    private String lastName;

    @Pattern(regexp = "^$|^\\+?[0-9]{10,15}$", message = "Некорректный номер телефона")
    private String phone;

    public UserProfileDto() {}

    public static UserProfileDto fromModel(UserModel user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        return dto;
    }

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
