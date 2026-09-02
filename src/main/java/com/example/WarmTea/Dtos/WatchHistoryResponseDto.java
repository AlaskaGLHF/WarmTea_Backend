package com.example.WarmTea.Dtos;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class WatchHistoryResponseDto {
    private Long id;
    private Long userId;
    private Long contentId;
    private String contentTitle;
    private String contentLogoUrl;
    private String contentType;
    private Integer contentReleaseYear;
    private Long episodeId;
    private String episodeTitle;
    private Integer seasonNumber;      // Добавлено
    private Integer episodeNumber;     // Добавлено
    private Integer stoppedAt;
    private Integer duration; // общая длительность контента/эпизода
    private OffsetDateTime watchedAt;
}