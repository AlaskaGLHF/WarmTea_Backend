package com.example.WarmTea.Service;

import com.example.WarmTea.Dtos.MovieDto;
import com.example.WarmTea.Dtos.MovieDto.MovieRequestDto;
import com.example.WarmTea.Dtos.MovieDto.MovieResponseDto;
import com.example.WarmTea.Models.Genre;
import com.example.WarmTea.Models.Movie;
import com.example.WarmTea.Models.MovieGenre;
import com.example.WarmTea.Models.MovieGenreKey;
import com.example.WarmTea.Repository.GenreRepository;
import com.example.WarmTea.Repository.MovieRepository;
import com.example.WarmTea.Repository.MovieTypeRepository;
import com.example.WarmTea.Utils.FileValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
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

    // 🔹 Получить все фильмы
    public List<MovieResponseDto> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // 🔹 Получить фильм по ID
    public MovieResponseDto getMovieById(Long id) {
        return movieRepository.findById(id)
                .map(this::toResponseDto)
                .orElse(null);
    }

    // 🔹 Создать фильм
    public MovieResponseDto createMovie(MovieRequestDto dto) {
        try {
            Movie movie = new Movie();
            movie.setKpId(dto.getKp_Id());
            movie.setTitle(dto.getTitle());
            movie.setDescription(dto.getDescription());
            movie.setShortDescription(dto.getShort_description());
            movie.setReleaseYear(dto.getReleaseYear());
            movie.setDuration(dto.getDuration());
            movie.setTypeNumber(dto.getType_number());
            movie.setStatus(dto.getStatus());
            movie.setRatingMpaa(dto.getRating_mpaa());
            movie.setAgeRating(dto.getAge_rating());
            movie.setRating(dto.getRating());
            movie.setCountry(dto.getCountry());
            movie.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : OffsetDateTime.now());
            movie.setUpdatedAt(dto.getUpdatedAt() != null ? dto.getUpdatedAt() : OffsetDateTime.now());

            Movie savedMovie = movieRepository.save(movie);

            // Загрузка файлов
            String rootFolder = dto.getType_number() == 1 ? "films" : "serials";
            String movieFolder = rootFolder + "/" + sanitizeFolderName(savedMovie.getTitle());

            if (dto.getLogoFile() != null && !dto.getLogoFile().isEmpty()) {
                FileValidator.validateFileExtension(dto.getLogoFile(), List.of("png", "jpg", "jpeg", "gif"));
                String logoUrl = s3Service.uploadFile(dto.getLogoFile(), movieFolder + "/logo/");
                savedMovie.setLogoUrl(logoUrl);
            }
            if (dto.getVideoFile() != null && !dto.getVideoFile().isEmpty()) {
                FileValidator.validateFileExtension(dto.getVideoFile(), List.of("mp4", "avi", "mkv"));
                String videoUrl = s3Service.uploadFile(dto.getVideoFile(), movieFolder + "/video/");
                savedMovie.setVideoUrl(videoUrl);
            }

            // Привязка жанров
            List<Genre> genres = genreRepository.findAllById(dto.getGenreIds());
            final Movie finalSavedMovie = savedMovie;

            List<MovieGenre> movieGenres = genres.stream()
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

        } catch (Exception e) {
            log.error("Ошибка при создании фильма: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при создании фильма: " + e.getMessage());
        }
    }

    // 🔹 Обновить фильм
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
            existing.setTypeNumber(dto.getType_number());
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

    // 🔹 Удалить фильм
    public boolean deleteMovie(Long id) {
        if (movieRepository.existsById(id)) {
            movieRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // 🔹 Получить фильмы по жанрам
    public List<MovieResponseDto> getMoviesByGenres(List<String> genreNames) {
        if (genreNames == null || genreNames.isEmpty()) return List.of();

        return movieRepository.findByGenreNames(genreNames)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // 🔹 Преобразование Movie → DTO с рейтингами из Кинопоиска
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
                .type_number(movie.getTypeNumber())
                .type(movie.getType() != null ? movie.getType().getName() : null)
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
