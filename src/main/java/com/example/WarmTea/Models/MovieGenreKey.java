package com.example.WarmTea.Models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// ===== MovieGenreKey =====
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenreKey implements Serializable {

    private Long movieId = 0L;
    private Long genreId = 0L;
}
