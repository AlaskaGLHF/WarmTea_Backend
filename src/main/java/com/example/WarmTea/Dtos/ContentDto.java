package com.example.WarmTea.Dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "DTO-модели для контента (фильмы, сериалы, аниме)")
public class ContentDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Полный ответ с информацией о контенте")
    public static class ContentResponseDto {

        @Schema(description = "Уникальный идентификатор", example = "1")
        private Long id;

        @Schema(description = "ID на Кинопоиске", example = "1234567")
        private Long Kp_Id;

        @Schema(description = "Название", example = "Inception")
        private String title;

        @Schema(description = "Полное описание", example = "A mind-bending thriller...")
        private String description;

        @Schema(description = "Короткое описание", example = "Dreams within dreams.")
        private String short_description;

        @Schema(description = "Год выпуска", example = "2010")
        private int releaseYear;

        @Schema(description = "Продолжительность (для фильма) или средняя длина серии", example = "148")
        private int duration;

        @Schema(description = "Тип контента", example = "MOVIE")
        private String type;

        @Schema(description = "Статус (Released, Ongoing)", example = "Released")
        private String status;

        @Schema(description = "Возрастное ограничение", example = "16")
        private int age_rating;

        @Schema(description = "Общий пользовательский рейтинг", example = "8.7")
        private double rating;

        @Schema(description = "Рейтинг Кинопоиска", example = "8.6")
        private double kp_rating;

        @Schema(description = "URL логотипа", example = "https://cdn.example.com/content/logo.png")
        private String logo_url;

        @Schema(description = "URL видео (только для фильмов)", example = "https://cdn.example.com/video.mp4")
        private String video_url;

        @Schema(description = "Страна производства", example = "USA")
        private String country;

        @Schema(description = "Список жанров", example = "[\"Action\", \"Sci-Fi\"]")
        private List<String> genres;

        @Schema(description = "Список эпизодов (для сериалов и аниме)")
        private List<EpisodeResponseDto> episodes;

        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Запрос на создание или обновление контента")
    public static class ContentRequestDto {

        private Long Kp_Id;
        private String title;
        private String description;
        private String short_description;
        private int releaseYear;
        private int duration;

        @Schema(description = "Тип контента (MOVIE, SERIES, ANIME)", example = "SERIES")
        private String type;

        private String status;
        private int age_rating;
        private double rating;
        private String country;

        private MultipartFile logoFile;
        private MultipartFile videoFile; // Только для MOVIE

        @Schema(description = "ID жанров", example = "[1, 2]")
        private List<Long> genreIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Краткая информация для плитки в каталоге")
    public static class ShortContentDto {
        private Long id;
        private String title;
        private String short_description;
        private int releaseYear;
        private String type;
        private double rating;
        private String logo_url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "DTO для эпизода")
    public static class EpisodeResponseDto {
        private Long id;
        private int seasonNumber;
        private int episodeNumber;
        private String title;
        private String video_url;
        private int duration;
    }
}