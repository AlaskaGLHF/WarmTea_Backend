package com.example.WarmTea.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrackDto {
    private String src;
    private String label;
    private String language;
    private String kind; // 'subtitles' или 'audio'
    private boolean defaultTrack;
}
