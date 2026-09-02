package com.example.WarmTea.Dtos;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class RatingResponseDto {
    private Long id;
    private Long userId;
    private String username;
    private Long contentId;
    private String contentTitle;
    private Integer score;
    private OffsetDateTime createdAt;
}