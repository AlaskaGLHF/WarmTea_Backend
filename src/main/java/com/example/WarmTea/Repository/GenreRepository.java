package com.example.WarmTea.Repository;

import com.example.WarmTea.Models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    // Поиск всех жанров по списку названий
    List<Genre> findAllByNameIn(List<String> names);
}
