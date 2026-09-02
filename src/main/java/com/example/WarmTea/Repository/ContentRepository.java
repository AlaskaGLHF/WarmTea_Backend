package com.example.WarmTea.Repository;

import com.example.WarmTea.Models.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {


    Optional<Content> findByTitle(String title);

    // Проверка существования фильма с указанным названием
    boolean existsByTitle(String title);

    // Поиск фильмов по списку названий жанров
    @Query("""
        SELECT DISTINCT m
        FROM Content m
        JOIN m.genresLinks mg
        JOIN mg.genre g
        WHERE g.name IN :genreNames
    """)
    List<Content> findByGenreNames(@Param("genreNames") List<String> genreNames);

    Page<Content> findAll(Pageable pageable);

    // ContentRepository.java
    @Query(value = "SELECT * FROM content WHERE type = :type ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Content> findRandomByType(@Param("type") String type, @Param("limit") int limit);

}
