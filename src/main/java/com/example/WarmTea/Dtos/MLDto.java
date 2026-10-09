package com.example.WarmTea.Dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "DTO для взаимодействия с ML-сервисом рекомендаций")
public class MLDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Запрос к ML-сервису на получение рекомендаций")
    public static class RecommendationRequestDto {
        @Schema(description = "ID пользователя", example = "123")
        private Long userId;

        @Schema(description = "Количество рекомендаций", example = "10")
        private int n;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Элемент рекомендации от ML-сервиса")
    public static class RecommendationItemDto {
        @Schema(description = "ID контента", example = "42")
        private Long id;

        @Schema(description = "Название", example = "Inception")
        private String title;

        @Schema(description = "Тип (MOVIE, SERIES, ANIME)", example = "MOVIE")
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Ответ от ML-сервиса со списком рекомендаций")
    public static class RecommendationResponseDto {
        @Schema(description = "Список рекомендаций")
        private List<RecommendationItemDto> recommendations;
    }
}