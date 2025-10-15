package com.example.WarmTea.Dtos;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

public class MovieDto {

    // === DTO для ответа при получении фильма ===
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MovieResponseDto {
        private Long id;
        private String title;
        private String description;
        private int releaseYear;
        private int duration;
        private double rating;
        private OffsetDateTime createdAt;
        private List<String> genres; // имена жанров
    }

    // === DTO для запроса создания/обновления фильма ===
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MovieRequestDto {
        private String title;
        private String description;
        private int releaseYear;
        private int duration;
        private double rating;
        private OffsetDateTime createdAt; // может быть null
        private List<Long> genreIds; // id жанров
    }
}
