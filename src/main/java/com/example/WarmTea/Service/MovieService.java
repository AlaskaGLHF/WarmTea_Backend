package com.example.WarmTea.Service;

import com.example.WarmTea.Dtos.MovieDto;
import com.example.WarmTea.Dtos.MovieDto.MovieRequestDto;
import com.example.WarmTea.Dtos.MovieDto.MovieResponseDto;
import com.example.WarmTea.Models.*;
import com.example.WarmTea.Repository.GenreRepository;
import com.example.WarmTea.Repository.MovieRepository;
import com.example.WarmTea.Repository.MovieTypeRepository;
import com.example.WarmTea.Utils.FileValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieTypeRepository movieTypeRepository;
    private final KinopoiskApiService kinopoiskApiService;
    private final S3Service s3Service;

    public MovieService(MovieRepository movieRepository,
                        GenreRepository genreRepository,
                        MovieTypeRepository movieTypeRepository,
                        KinopoiskApiService kinopoiskApiService,
                        S3Service s3Service) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.movieTypeRepository = movieTypeRepository;
        this.kinopoiskApiService = kinopoiskApiService;
        this.s3Service = s3Service;
    }

    // === Получить все фильмы ===
    public List<MovieResponseDto> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // === Получить фильм по ID ===
    public MovieResponseDto getMovieById(Long id) {
        return movieRepository.findById(id)
                .map(this::toResponseDto)
                .orElse(null);
    }

    // === Создать фильм ===
    @Transactional
    public MovieResponseDto createMovie(MovieRequestDto dto) {
        try {
            log.info("=== START создания фильма: {}", dto.getTitle());

            log.info("1. Получение типа фильма с type_number={}", dto.getType_number());
            MovieType movieType = getMovieType(dto.getType_number());
            log.info("Тип фильма найден: {}", movieType.getName());

            log.info("2. Создание сущности Movie");
            Movie movie = buildMovieEntity(dto, movieType);

            log.info("3. Загрузка файлов на S3");
            uploadFilesToS3(dto, movie);
            log.info("Файлы загружены: logoUrl={}, videoUrl={}", movie.getLogoUrl(), movie.getVideoUrl());

            log.info("4. Сохранение фильма без жанров");
            Movie savedMovie = movieRepository.save(movie);
            log.info("Фильм сохранён с id={}", savedMovie.getId());

            if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
                log.info("5. Привязка жанров к фильму: {}", dto.getGenreIds());
                attachGenresToMovie(savedMovie, dto.getGenreIds());
                log.info("Жанры успешно привязаны");
            }

            log.info("=== END создания фильма: {}", dto.getTitle());
            return toResponseDto(savedMovie);

        } catch (Exception e) {
            log.error("Ошибка при создании фильма: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при создании фильма: " + e.getMessage(), e);
        }
    }

    private MovieType getMovieType(Integer typeNumber) {
        if (typeNumber == null || typeNumber <= 0) {
            throw new RuntimeException("Не указан или некорректный тип фильма");
        }
        return movieTypeRepository.findById(Long.valueOf(typeNumber))
                .orElseThrow(() -> new RuntimeException("Тип фильма с id " + typeNumber + " не найден"));
    }

    private Movie buildMovieEntity(MovieRequestDto dto, MovieType type) {
        return Movie.builder()
                .kpId(dto.getKp_Id())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .shortDescription(dto.getShort_description())
                .releaseYear(dto.getReleaseYear())
                .duration(dto.getDuration())
                .status(dto.getStatus())
                .ratingMpaa(dto.getRating_mpaa())
                .ageRating(dto.getAge_rating())
                .rating(dto.getRating())
                .country(dto.getCountry())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .movieGenres(new ArrayList<>())
                .type(type)
                .build();
    }

    private void uploadFilesToS3(MovieRequestDto dto, Movie movie) {
        String folderRoot = movie.getType().getId() == 1 ? "films" : "serials";
        String folder = folderRoot + "/" + sanitizeFolderName(movie.getTitle());

        if (dto.getLogoFile() != null && !dto.getLogoFile().isEmpty()) {
            log.info("Загрузка логотипа фильма на S3");
            FileValidator.validateFileExtension(dto.getLogoFile(), List.of("png", "jpg", "jpeg"));
            movie.setLogoUrl(s3Service.uploadFile(dto.getLogoFile(), folder + "/logo/"));
        }
        if (dto.getVideoFile() != null && !dto.getVideoFile().isEmpty()) {
            log.info("Загрузка видео фильма на S3");
            FileValidator.validateFileExtension(dto.getVideoFile(), List.of("mp4", "mkv", "avi"));
            movie.setVideoUrl(s3Service.uploadFile(dto.getVideoFile(), folder + "/video/"));
        }
    }

    private void attachGenresToMovie(Movie movie, List<Long> genreIds) {
        List<Genre> genres = genreRepository.findAllById(genreIds);
        for (Genre genre : genres) {
            MovieGenre mg = new MovieGenre();
            mg.setId(new MovieGenreKey(movie.getId(), genre.getId()));
            mg.setMovie(movie);
            mg.setGenre(genre);
            movie.getMovieGenres().add(mg);
        }
        movieRepository.save(movie);
    }

    // === Обновить фильм ===
    public MovieResponseDto updateMovie(Long id, MovieRequestDto dto) {
        try {
            Optional<Movie> existingOpt = movieRepository.findById(id);
            if (existingOpt.isEmpty()) return null;

            Movie existing = existingOpt.get();
            existing.setKpId(dto.getKp_Id());
            existing.setTitle(dto.getTitle());
            existing.setDescription(dto.getDescription());
            existing.setShortDescription(dto.getShort_description());
            existing.setReleaseYear(dto.getReleaseYear());
            existing.setDuration(dto.getDuration());
            existing.setType(getMovieType(dto.getType_number()));
            existing.setStatus(dto.getStatus());
            existing.setRatingMpaa(dto.getRating_mpaa());
            existing.setAgeRating(dto.getAge_rating());
            existing.setRating(dto.getRating());
            existing.setCountry(dto.getCountry());
            existing.setUpdatedAt(OffsetDateTime.now());

            String rootFolder = dto.getType_number() == 1 ? "films" : "serials";
            String movieFolder = rootFolder + "/" + sanitizeFolderName(existing.getTitle());

            if (dto.getLogoFile() != null && !dto.getLogoFile().isEmpty()) {
                FileValidator.validateFileExtension(dto.getLogoFile(), List.of("png", "jpg", "jpeg", "gif"));
                String logoUrl = s3Service.uploadFile(dto.getLogoFile(), movieFolder + "/logo/");
                existing.setLogoUrl(logoUrl);
            }
            if (dto.getVideoFile() != null && !dto.getVideoFile().isEmpty()) {
                FileValidator.validateFileExtension(dto.getVideoFile(), List.of("mp4", "avi", "mkv"));
                String videoUrl = s3Service.uploadFile(dto.getVideoFile(), movieFolder + "/video/");
                existing.setVideoUrl(videoUrl);
            }

            // 🔗 Обновление жанров
            existing.getMovieGenres().clear();
            List<Genre> genres = genreRepository.findAllById(dto.getGenreIds());
            List<MovieGenre> movieGenres = genres.stream()
                    .map(genre -> {
                        MovieGenre mg = new MovieGenre();
                        mg.setId(new MovieGenreKey(existing.getId(), genre.getId()));
                        mg.setMovie(existing);
                        mg.setGenre(genre);
                        return mg;
                    })
                    .collect(Collectors.toList());

            existing.setMovieGenres(movieGenres);
            Movie updated = movieRepository.save(existing);

            return toResponseDto(updated);

        } catch (Exception e) {
            log.error("Ошибка при обновлении фильма: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при обновлении фильма: " + e.getMessage());
        }
    }

    // === Удалить фильм ===
    public boolean deleteMovie(Long id) {
        if (movieRepository.existsById(id)) {
            movieRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // === Получить фильмы по жанрам ===
    public List<MovieResponseDto> getMoviesByGenres(List<String> genreNames) {
        if (genreNames == null || genreNames.isEmpty()) return List.of();

        return movieRepository.findByGenreNames(genreNames)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // === Преобразование Movie → DTO ===
    private MovieResponseDto toResponseDto(Movie movie) {
        List<String> genreNames = movie.getMovieGenres().stream()
                .map(mg -> mg.getGenre().getName())
                .toList();

        Optional<KinopoiskApiService.Ratings> ratingsOpt =
                (movie.getKpId() != null && movie.getKpId() > 0)
                        ? Optional.ofNullable(kinopoiskApiService.getMovie(movie.getKpId()))
                        .map(KinopoiskApiService.MovieApiResponse::getRating)
                        : Optional.empty();

        return MovieResponseDto.builder()
                .id(movie.getId())
                .Kp_Id(movie.getKpId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .short_description(movie.getShortDescription())
                .releaseYear(movie.getReleaseYear())
                .duration(movie.getDuration())
                .type_number(movie.getType() != null ? movie.getType().getId().intValue() : 0)
                .type(movie.getType().getName())
                .status(movie.getStatus())
                .rating_mpaa(movie.getRatingMpaa())
                .age_rating(movie.getAgeRating())
                .rating(movie.getRating())
                .kp_rating(ratingsOpt.map(r -> r.getKp() != null ? r.getKp() : 0).orElse(0.0))
                .imdb_rating(ratingsOpt.map(r -> r.getImdb() != null ? r.getImdb() : 0).orElse(0.0))
                .logo_url(movie.getLogoUrl())
                .video_url(movie.getVideoUrl())
                .country(movie.getCountry())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .genres(genreNames)
                .build();
    }

    private String sanitizeFolderName(String title) {
        return title.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    }
}
