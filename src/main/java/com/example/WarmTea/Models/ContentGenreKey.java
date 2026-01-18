package com.example.WarmTea.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

// ===== MovieGenreKey =====
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentGenreKey implements Serializable {
    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "genre_id")
    private Long genreId;
}

