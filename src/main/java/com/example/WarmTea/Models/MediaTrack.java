package com.example.WarmTea.Models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "media_tracks")
@Data
public class MediaTrack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "track_type")
    private String type; // audio или subtitles

    private String label; // Название для плеера

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "src_url", columnDefinition = "text")
    private String srcUrl;

    @Column(name = "is_default")
    private boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id")
    private Episode episode;

    @ManyToOne
    @JoinColumn(name = "content_id") // имя колонки в БД
    private Content content; // Имя поля должно быть "content"

}