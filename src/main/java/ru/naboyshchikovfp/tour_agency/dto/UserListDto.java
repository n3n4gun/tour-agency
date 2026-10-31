package ru.naboyshchikovfp.tour_agency.dto;

import ru.naboyshchikovfp.tour_agency.models.UserModel;
import ru.naboyshchikovfp.tour_agency.models.UserRole;

public record UserListDto(
    Long id,
    String email,
    String firstName,
    String lastName,
    UserRole role,
    boolean enabled
) {
    public static UserListDto fromModel(UserModel user) {
        return new UserListDto(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            user.isEnabled()
        );
    }
}
