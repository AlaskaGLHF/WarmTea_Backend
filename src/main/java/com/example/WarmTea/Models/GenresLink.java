package com.example.WarmTea.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// ===== MovieGenre =====
@Entity
@Table(name = "genres_link")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenresLink {

    @EmbeddedId
    private ContentGenreKey id;

    @ManyToOne
    @MapsId("contentId")
    @JoinColumn(name = "content_id")
    private Content content;

    @ManyToOne
    @MapsId("genreId")
    @JoinColumn(name = "genre_id")
    private Genre genre;
}


