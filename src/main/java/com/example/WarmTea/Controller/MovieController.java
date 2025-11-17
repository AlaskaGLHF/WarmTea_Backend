package com.example.WarmTea.Controller;

import com.example.WarmTea.Dtos.MovieDto;
import com.example.WarmTea.Service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    // === Получить все фильмы ===
    @Operation(summary = "Получить все фильмы", description = "Возвращает список всех фильмов в базе данных")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение списка фильмов",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MovieDto.MovieResponseDto.class)
                    )
            )
    })
    @GetMapping
    public List<MovieDto.MovieResponseDto> getAllMovies() {
        return movieService.getAllMovies();
    }

    // === Получить фильм по ID ===
    @Operation(summary = "Получить фильм по ID", description = "Возвращает информацию о фильме по его ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Фильм найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MovieDto.MovieResponseDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Фильм не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<MovieDto.MovieResponseDto> getMovieById(@PathVariable Long id) {
        MovieDto.MovieResponseDto movie = movieService.getMovieById(id);
        return movie != null ? ResponseEntity.ok(movie) : ResponseEntity.notFound().build();
    }

    // === Создать фильм ===
    @Operation(
            summary = "Создать новый фильм",
            description = "Добавляет новый фильм с файлами и записывает URL в базу данных"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Фильм успешно создан",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MovieDto.MovieResponseDto.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content)
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MovieDto.MovieResponseDto> createMovie(
            @Valid @ModelAttribute MovieDto.MovieRequestDto dto
    ) throws IOException {

        MovieDto.MovieResponseDto createdMovie = movieService.createMovie(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdMovie);
    }

    // === Обновить фильм ===
    @Operation(summary = "Обновить данные фильма", description = "Редактирует существующий фильм по ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Фильм успешно обновлён",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MovieDto.MovieResponseDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Фильм не найден", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<MovieDto.MovieResponseDto> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieDto.MovieRequestDto dto
    ) throws IOException {
        MovieDto.MovieResponseDto updated = movieService.updateMovie(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // === Удалить фильм ===
    @Operation(summary = "Удалить фильм", description = "Удаляет фильм по его идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Фильм успешно удалён"),
            @ApiResponse(responseCode = "404", description = "Фильм не найден", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        return movieService.deleteMovie(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // === Поиск фильмов по жанрам ===
    @Operation(summary = "Поиск фильмов по жанрам", description = "Позволяет искать фильмы по жанрам через запятую (например: action,comedy)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список найденных фильмов",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MovieDto.MovieResponseDto.class)
                    )
            )
    })
    @GetMapping("/search")
    public List<MovieDto.MovieResponseDto> searchMoviesByGenres(@RequestParam String genres) {
        List<String> genreList = Arrays.stream(genres.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        return movieService.getMoviesByGenres(genreList);
    }
}
