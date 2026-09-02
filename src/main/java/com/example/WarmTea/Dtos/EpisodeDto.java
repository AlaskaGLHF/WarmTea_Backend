package com.example.WarmTea.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeDto {
    private Long id;
    private Long contentId;
    private int seasonNumber;
    private int episodeNumber;
    private String title;
    private String videoUrl;
    private int duration;
    private String thumbnail;
}