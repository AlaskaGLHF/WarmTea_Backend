package com.example.WarmTea.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movie")
@Data // ✅ Lombok: геттеры, сеттеры, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private int release_year;

    private int duration;

    private double rating;

    private OffsetDateTime created_at = OffsetDateTime.now();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<MovieGenre> movieGenres = new ArrayList<>();
}
