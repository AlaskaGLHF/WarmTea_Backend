package com.example.WarmTea.Dtos;

import lombok.*;
import java.time.OffsetDateTime;

public class UsersDto {

    // === DTO для ответа при получении пользователя ===
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserResponseDTO {
        private Long id;
        private String username;
        private String email;
        private OffsetDateTime createdAt;
    }

    // === DTO для запроса создания/обновления пользователя ===
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserRequestDTO {
        private String username;
        private String email;
        private String password;
    }

    // === DTO для запроса логина ===
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequestDTO {
        private String username;
        private String password;
    }

    // === DTO для ответа при логине ===
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginResponseDTO {
        private String token;
    }
}
