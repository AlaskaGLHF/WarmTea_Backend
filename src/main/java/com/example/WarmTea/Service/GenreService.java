package com.example.WarmTea.Service;

import com.example.WarmTea.Dtos.GenreDto;
import com.example.WarmTea.Models.Genre;
import com.example.WarmTea.Repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    public List<GenreDto> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                .collect(Collectors.toList());
    }

    public GenreDto createGenre(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Название жанра обязательно");
        }
        Genre genre = new Genre();
        genre.setName(name.trim());
        Genre saved = genreRepository.save(genre);
        return new GenreDto(saved.getId(), saved.getName());
    }
}