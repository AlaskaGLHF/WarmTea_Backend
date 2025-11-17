package com.example.WarmTea.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

// ===== MovieGenreKey =====
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenreKey implements Serializable {

    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "genre_id")
    private Long genreId;
}


