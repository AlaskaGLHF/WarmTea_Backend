package com.example.WarmTea.Models;

import com.example.WarmTea.Enums.ContentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long kpId;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String shortDescription;

    @Enumerated(EnumType.STRING)
    private ContentType type; // MOVIE, SERIES, ANIME

    private Double rating;

    // Новое поле для разделения логики Фильм/Сериал
    @Column(name = "is_single_video")
    private boolean isSingleVideo;

    private int releaseYear;
    private int duration; // Для фильма - общая, для сериала - средняя серии
    private int ageRating;
    private String status;
    private String logoUrl;
    private String videoUrl; // Заполняется только если isSingleVideo == true
    private String country;

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seasonNumber ASC, episodeNumber ASC")
    @Builder.Default
    private List<Episode> episodes = new ArrayList<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GenresLink> genresLinks = new ArrayList<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Favorite> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WatchHistory> watchHistories = new ArrayList<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "content")
    private List<MediaTrack> mediaTracks;

    // Автоматическое обновление даты при изменении
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}