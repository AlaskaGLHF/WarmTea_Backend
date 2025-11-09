package com.example.WarmTea.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;       // Логин
    private String email;          // Email
    private String passwordHash;   // Хеш пароля
    private String firstName;      // Имя
    private String lastName;       // Фамилия
    private OffsetDateTime dateOfBirth; // Дата рождения
    private String country;        // Страна
    private String avatarUrl;      // Ссылка на аватар

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Roles role;             // Связь с таблицей ролей

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // ---------- Избранные фильмы ----------
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Favorite> favorites = new ArrayList<>();

    // ---------- История просмотров ----------
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WatchHistory> watchHistories = new ArrayList<>();

    // ---------- Рейтинги ----------
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Rating> ratings = new ArrayList<>();

    // ---------- Refresh токены ----------
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();
}
