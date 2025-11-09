package com.example.WarmTea.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long kpId;          // ID на Кинопоиске (может быть null)
    private String title;         // Название фильма
    private String description;   // Полное описание
    private String shortDescription; // Короткое описание для карточек
    private int releaseYear;      // Год выпуска
    private int duration;         // Длительность в минутах
    private double rating;        // Средний рейтинг на сайте
    private String ratingMpaa;    // PG-13, R и т.д.
    private int ageRating;        // Ограничение по возрасту
    private String status;        // completed / ongoing / announced
    private String logoUrl;       // Ссылка на постер
    private String videoUrl;      // Ссылка на видео в облаке
    private String country;       // Страна производства

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id")
    private MovieType type;       // Связь с таблицей movie_types

    private int typeNumber; // Номер сезона/часть

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // ---------- Связь с жанрами (M:N через промежуточную сущность MovieGenre) ----------
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MovieGenre> movieGenres = new ArrayList<>();

    // ---------- Связь с избранным (M:N через Favorite) ----------
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Favorite> favorites = new ArrayList<>();

    // ---------- Связь с историей просмотров ----------
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WatchHistory> watchHistories = new ArrayList<>();

    // ---------- Связь с рейтингами ----------
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Rating> ratings = new ArrayList<>();
}
