package com.example.WarmTea.Dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Schema(description = "DTO-классы для аутентификации и регистрации пользователей")
public class AuthDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Запрос на вход в систему")
    public static class LoginRequestDTO {

        @Schema(description = "Логин пользователя", example = "string")
        private String username;

        @Schema(description = "Пароль пользователя", example = "string")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Ответ на успешную авторизацию")
    public static class LoginResponseDTO {

        @Schema(description = "JWT-токен доступа", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String accessToken;
        private String refreshToken;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Запрос на регистрацию пользователя")
    public static class RegisterRequestDTO {

        @Schema(description = "Имя пользователя (уникальный username)", example = "john_doe")
        private String username;

        @Schema(description = "Email пользователя", example = "john@example.com")
        private String email;

        @Schema(description = "Пароль пользователя", example = "MyStrongP@ssw0rd")
        private String password;

        @Schema(description = "Имя пользователя", example = "John")
        private String firstName;

        @Schema(description = "Фамилия пользователя", example = "Doe")
        private String lastName;

        @Schema(description = "Страна пользователя", example = "USA")
        private String country;

        @Schema(description = "URL аватара (если загружен ранее)", example = "https://cdn.example.com/avatars/john_doe.jpg")
        private String avatarUrl;

        @Schema(description = "Дата рождения пользователя", example = "1998-04-15T00:00:00Z")
        private OffsetDateTime dateOfBirth;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Ответ после успешной регистрации")
    public static class RegisterResponseDTO {

        @Schema(description = "JWT-токен доступа")
        private String accessToken;

        @Schema(description = "JWT-токен обновления")
        private String refreshToken;
    }

}
