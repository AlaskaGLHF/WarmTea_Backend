package com.example.WarmTea.Dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "DTO-модели для фильмов")
public class MovieDto {

    // === DTO для ответа при получении фильма ===
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Ответ с информацией о фильме")
    public static class MovieResponseDto {

        @Schema(description = "Уникальный идентификатор фильма", example = "1")
        private Long id;

        @Schema(description = "ID фильма на Кинопоиске", example = "1234567")
        private Long Kp_Id;

        @Schema(description = "Название фильма", example = "Inception")
        private String title;

        @Schema(description = "Полное описание фильма", example = "A mind-bending thriller where dreams and reality intertwine.")
        private String description;

        @Schema(description = "Короткое описание фильма", example = "Dreams within dreams.")
        private String short_description;

        @Schema(description = "Год выпуска", example = "2010")
        private int releaseYear;

        @Schema(description = "Продолжительность фильма в минутах", example = "148")
        private int duration;

        @Schema(description = "Числовой код типа (например, 1 — фильм, 2 — сериал)", example = "1")
        private int type_number;

        @Schema(description = "Тип контента", example = "Movie")
        private String type;

        @Schema(description = "Статус фильма (например, Released, Upcoming)", example = "Released")
        private String status;

        @Schema(description = "Рейтинг MPAA", example = "PG-13")
        private String rating_mpaa;

        @Schema(description = "Возрастное ограничение", example = "16")
        private int age_rating;

        @Schema(description = "Общий пользовательский рейтинг", example = "8.7")
        private double rating;

        @Schema(description = "Рейтинг Кинопоиска", example = "8.6")
        private double kp_rating;

        @Schema(description = "Рейтинг IMDb", example = "8.8")
        private double imdb_rating;

        @Schema(description = "URL постера или логотипа фильма", example = "https://cdn.example.com/movies/inception/logo.png")
        private String logo_url;

        @Schema(description = "URL видео или трейлера", example = "https://cdn.example.com/movies/inception/trailer.mp4")
        private String video_url;

        @Schema(description = "Страна производства фильма", example = "USA")
        private String country;

        @Schema(description = "Дата создания записи", example = "2024-06-15T10:30:00Z")
        private OffsetDateTime createdAt;

        @Schema(description = "Дата последнего обновления записи", example = "2024-07-01T12:00:00Z")
        private OffsetDateTime updatedAt;

        @Schema(description = "Список жанров фильма", example = "[\"Action\", \"Sci-Fi\", \"Thriller\"]")
        private List<String> genres;
    }

    // === DTO для запроса создания/обновления фильма ===
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Запрос на создание или обновление фильма")
    public static class MovieRequestDto {

        @Schema(description = "ID фильма на Кинопоиске", example = "1234567")
        private Long Kp_Id;

        @Schema(description = "Название фильма", example = "Inception")
        private String title;

        @Schema(description = "Полное описание фильма", example = "A mind-bending thriller where dreams and reality intertwine.")
        private String description;

        @Schema(description = "Короткое описание фильма", example = "Dreams within dreams.")
        private String short_description;

        @Schema(description = "Год выпуска", example = "2010")
        private int releaseYear;

        @Schema(description = "Продолжительность фильма в минутах", example = "148")
        private int duration;

        @Schema(description = "Числовой код типа", example = "1")
        private int type_number;

        @Schema(description = "Тип контента (Movie, Series и т.д.)", example = "Movie")
        private String type;

        @Schema(description = "Статус фильма (Released, In Production, Upcoming и т.д.)", example = "Released")
        private String status;

        @Schema(description = "Рейтинг MPAA", example = "PG-13")
        private String rating_mpaa;

        @Schema(description = "Возрастное ограничение", example = "16")
        private int age_rating;

        @Schema(description = "Пользовательский рейтинг", example = "8.7")
        private double rating;

        @Schema(description = "Файл постера или логотипа фильма (изображение)")
        private MultipartFile logoFile;

        @Schema(description = "Файл видео или трейлера фильма")
        private MultipartFile videoFile;

        @Schema(description = "Страна производства фильма", example = "USA")
        private String country;

        @Schema(description = "Дата создания фильма", example = "2024-06-15T10:30:00Z")
        private OffsetDateTime createdAt;

        @Schema(description = "Дата последнего обновления фильма", example = "2024-07-01T12:00:00Z")
        private OffsetDateTime updatedAt;

        @Schema(description = "Список идентификаторов жанров фильма", example = "[1, 2, 3]")
        private List<Long> genreIds; // id жанров
    }
}
