package com.example.WarmTea.Service;

import com.example.WarmTea.Dtos.MovieDto;
import com.example.WarmTea.Dtos.MovieDto.MovieRequestDto;
import com.example.WarmTea.Dtos.MovieDto.MovieResponseDto;
import com.example.WarmTea.Models.Movie;
import com.example.WarmTea.Models.MovieGenre;
import com.example.WarmTea.Models.MovieGenreKey;
import com.example.WarmTea.Repository.GenreRepository;
import com.example.WarmTea.Repository.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j // ✅ Lombok для логирования
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    public MovieService(MovieRepository movieRepository, GenreRepository genreRepository) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
    }

    public List<MovieDto.MovieResponseDto> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public MovieDto.MovieResponseDto getMovieById(Long id) {
        return movieRepository.findById(id)
                .map(this::toResponseDto) // твой метод маппинга
                .orElse(null);
    }

    public MovieResponseDto createMovie(MovieDto.MovieRequestDto dto) {
        Movie movie = new Movie();
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setRelease_year(dto.getReleaseYear());
        movie.setDuration(dto.getDuration());
        movie.setRating(dto.getRating());
        movie.setCreated_at(dto.getCreatedAt() != null ? dto.getCreatedAt() : OffsetDateTime.now());

        // Сохраняем фильм
        Movie savedMovie = movieRepository.save(movie);

        // Связи с жанрами
        var genres = genreRepository.findAllById(dto.getGenreIds());
        Movie finalSavedMovie = savedMovie;
        var movieGenres = genres.stream()
                .map(genre -> {
                    MovieGenre mg = new MovieGenre();
                    mg.setId(new MovieGenreKey(finalSavedMovie.getId(), genre.getId()));
                    mg.setMovie(finalSavedMovie);
                    mg.setGenre(genre);
                    return mg;
                })
                .collect(Collectors.toList());

        savedMovie.setMovieGenres(movieGenres);
        savedMovie = movieRepository.save(savedMovie);

        return toResponseDto(savedMovie);
    }

    public MovieResponseDto updateMovie(Long id, MovieRequestDto dto) {
        Optional<Movie> existingOpt = movieRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return null;
        }

        Movie existing = existingOpt.get();
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setRelease_year(dto.getReleaseYear());
        existing.setDuration(dto.getDuration());
        existing.setRating(dto.getRating());

        var genres = genreRepository.findAllById(dto.getGenreIds());
        var movieGenres = genres.stream()
                .map(genre -> {
                    MovieGenre mg = new MovieGenre();
                    mg.setId(new MovieGenreKey(id, genre.getId()));
                    mg.setMovie(existing);
                    mg.setGenre(genre);
                    return mg;
                })
                .collect(Collectors.toList());

        existing.setMovieGenres(movieGenres);

        Movie updated = movieRepository.save(existing);
        return toResponseDto(updated);
    }

    public boolean deleteMovie(Long id) {
        if (movieRepository.existsById(id)) {
            movieRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<MovieResponseDto> getMoviesByGenres(List<String> genreNames) {
        if (genreNames == null || genreNames.isEmpty()) {
            return List.of();
        }
        return movieRepository.findByGenreNames(genreNames)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // === Маппинг на DTO ===
    private MovieResponseDto toResponseDto(Movie movie) {
        List<String> genreNames = movie.getMovieGenres() == null
                ? List.of()
                : movie.getMovieGenres().stream()
                .map(mg -> mg.getGenre() != null ? mg.getGenre().getName() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new MovieResponseDto(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getRelease_year(),
                movie.getDuration(),
                movie.getRating(),
                movie.getCreated_at(),
                genreNames
        );
    }
}
