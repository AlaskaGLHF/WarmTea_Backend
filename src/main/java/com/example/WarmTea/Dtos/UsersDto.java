package com.example.WarmTea.Dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.OffsetDateTime;

@Schema(description = "DTO-модели пользователя")
public class UsersDto {

    // === DTO для ответа ===
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Ответ с информацией о пользователе")
    public static class UserResponseDTO {

        @Schema(description = "Уникальный идентификатор пользователя", example = "1")
        private Long id;

        @Schema(description = "Имя пользователя (логин)", example = "warm_tea_fan")
        private String username;

        @Schema(description = "Электронная почта пользователя", example = "user@example.com")
        private String email;

        @Schema(description = "Имя пользователя", example = "John")
        private String firstName;

        @Schema(description = "Фамилия пользователя", example = "Doe")
        private String lastName;

        @Schema(description = "Страна пользователя", example = "Finland")
        private String country;

        @Schema(description = "URL аватара пользователя", example = "https://cdn.example.com/avatars/user123.png")
        private String avatarUrl;

        @Schema(description = "Роль пользователя в системе", example = "USER")
        private String roleName;

        @Schema(description = "Дата рождения пользователя", example = "1998-05-20T00:00:00Z")
        private OffsetDateTime dateOfBirth;

        @Schema(description = "Дата создания учетной записи", example = "2024-01-15T12:00:00Z")
        private OffsetDateTime createdAt;

        @Schema(description = "Дата последнего обновления учетной записи", example = "2024-06-10T08:30:00Z")
        private OffsetDateTime updatedAt;

        @Schema(description = "Удалён ли аккаунт пользователя", example = "False")
        private Boolean is_delete;

        private String token;
    }

    // === DTO для запроса ===
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Запрос для обновления данных пользователя")
    public static class UserRequestDTO {

        @Schema(description = "Никнейм пользователя", example = "AleshaPopovich")
        private String username;

        @Schema(description = "Электронная почта пользователя", example = "new_email@example.com")
        private String email;

        @Schema(description = "Имя пользователя", example = "John")
        private String firstName;

        @Schema(description = "Фамилия пользователя", example = "Doe")
        private String lastName;

        @Schema(description = "Страна пользователя", example = "Finland")
        private String country;

        @Schema(description = "URL аватара пользователя", example = "https://cdn.example.com/avatars/user_new.png")
        private String avatarUrl;

        @Schema(description = "Дата рождения пользователя", example = "1998-05-20T00:00:00Z")
        private OffsetDateTime dateOfBirth;
    }

}
