package com.example.WarmTea.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "episodes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    private int seasonNumber;  // Тот самый номер сезона
    private int episodeNumber; // Номер серии
    private String title;      // Название серии (если есть)
    private String videoUrl;   // Ссылка на видео конкретной серии
    private int duration;      // Длительность серии

    private OffsetDateTime createdAt = OffsetDateTime.now();
}