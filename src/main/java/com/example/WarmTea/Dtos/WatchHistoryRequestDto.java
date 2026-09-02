package com.example.WarmTea.Dtos;

import lombok.Data;

@Data
public class WatchHistoryRequestDto {
    private Long contentId;
    private Long episodeId; // null для фильмов
    private Integer stoppedAt; // секунды
}