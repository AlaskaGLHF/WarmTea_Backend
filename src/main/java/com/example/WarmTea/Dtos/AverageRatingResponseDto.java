package com.example.WarmTea.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AverageRatingResponseDto {
    private Long contentId;
    private Double averageScore;
    private Long votesCount;
}