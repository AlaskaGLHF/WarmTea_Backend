package com.example.WarmTea.Models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "genre")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = 0L;

    @Column(nullable = false, unique = true)
    private String name;

    // Обратная связь с MovieGenre
    @OneToMany(mappedBy = "genre", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MovieGenre> movieGenres = new ArrayList<>();
}
