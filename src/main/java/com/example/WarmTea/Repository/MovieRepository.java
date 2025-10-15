package com.example.WarmTea.Repository;

import com.example.WarmTea.Models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Поиск фильма по названию
    Optional<Movie> findByTitle(String title);

    // Проверка существования фильма с указанным названием
    boolean existsByTitle(String title);

    // Поиск фильмов по списку названий жанров
    @Query("""
        SELECT DISTINCT m
        FROM Movie m
        JOIN m.movieGenres mg
        JOIN mg.genre g
        WHERE g.name IN :genreNames
    """)
    List<Movie> findByGenreNames(@Param("genreNames") List<String> genreNames);
}
