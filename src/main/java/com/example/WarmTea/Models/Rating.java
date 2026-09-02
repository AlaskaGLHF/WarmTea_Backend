package com.example.WarmTea.Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "rating", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Content content;

    private int score;
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
