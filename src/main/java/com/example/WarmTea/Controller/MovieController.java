package com.example.WarmTea.Controller;

import com.example.WarmTea.Dtos.MovieDto;
import com.example.WarmTea.Service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor // Lombok генерирует конструктор с final полями
public class MovieController {

    private final MovieService movieService;

    // === Получить все фильмы ===
    @GetMapping
    public List<MovieDto.MovieResponseDto> getAllMovies() {
        return movieService.getAllMovies();
    }

    // === Получить фильм по ID ===
    @GetMapping("/{id}")
    public ResponseEntity<MovieDto.MovieResponseDto> getMovieById(@PathVariable Long id) {
        MovieDto.MovieResponseDto movie = movieService.getMovieById(id);
        return movie != null ? ResponseEntity.ok(movie) : ResponseEntity.notFound().build();
    }

    // === Создать фильм ===
    @PostMapping
    public ResponseEntity<MovieDto.MovieResponseDto> createMovie(
            @Valid @RequestBody MovieDto.MovieRequestDto dto) {
        MovieDto.MovieResponseDto created = movieService.createMovie(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // === Обновить фильм ===
    @PutMapping("/{id}")
    public ResponseEntity<MovieDto.MovieResponseDto> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieDto.MovieRequestDto dto) {
        MovieDto.MovieResponseDto updated = movieService.updateMovie(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // === Удалить фильм ===
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        return movieService.deleteMovie(id) ? ResponseEntity.noContent().build() : ResponseEntity.ok().build();
    }

    // === Поиск фильмов по жанрам (можно несколько через запятую) ===
    @GetMapping("/search")
    public List<MovieDto.MovieResponseDto> searchMoviesByGenres(@RequestParam String genres) {
        List<String> genreList = Arrays.stream(genres.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        return movieService.getMoviesByGenres(genreList);
    }
}
