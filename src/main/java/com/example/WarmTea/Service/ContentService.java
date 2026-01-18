package com.example.WarmTea.Service;

import com.example.WarmTea.Dtos.ContentDto;
import com.example.WarmTea.Dtos.ContentDto.ContentRequestDto;
import com.example.WarmTea.Dtos.ContentDto.ContentResponseDto;
import com.example.WarmTea.Models.*;
import com.example.WarmTea.Enums.ContentType;
import com.example.WarmTea.Repository.GenreRepository;
import com.example.WarmTea.Repository.ContentRepository;
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
public class ContentService {

    private final ContentRepository contentRepository;
    private final GenreRepository genreRepository;
    private final KinopoiskApiService kinopoiskApiService;
    private final S3Service s3Service;

    public ContentService(ContentRepository contentRepository,
                          GenreRepository genreRepository,
                          KinopoiskApiService kinopoiskApiService,
                          S3Service s3Service) {
        this.contentRepository = contentRepository;
        this.genreRepository = genreRepository;
        this.kinopoiskApiService = kinopoiskApiService;
        this.s3Service = s3Service;
    }

    public List<ContentResponseDto> getAllContent() {
        return contentRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<ContentDto.ShortContentDto> getAllContentShort() {
        return contentRepository.findAll().stream()
                .map(content -> ContentDto.ShortContentDto.builder()
                        .id(content.getId())
                        .title(content.getTitle())
                        .logo_url(content.getLogoUrl())
                        .short_description(content.getShortDescription())
                        .rating(content.getRating())
                        .releaseYear(content.getReleaseYear())
                        .type(content.getType().name())
                        .build())
                .toList();
    }

    public ContentResponseDto getContentById(Long id) {
        return contentRepository.findById(id)
                .map(this::toResponseDto)
                .orElse(null);
    }

    @Transactional
    public ContentResponseDto createContent(ContentRequestDto dto) {
        try {
            Content content = buildContentEntity(dto);
            uploadFilesToS3(dto, content);

            Content savedContent = contentRepository.save(content);

            if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
                attachGenresToContent(savedContent, dto.getGenreIds());
            }

            return toResponseDto(savedContent);
        } catch (Exception e) {
            log.error("Error creating content: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating content: " + e.getMessage(), e);
        }
    }

    private Content buildContentEntity(ContentRequestDto dto) {
        return Content.builder()
                .kpId(dto.getKp_Id())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .shortDescription(dto.getShort_description())
                .releaseYear(dto.getReleaseYear())
                .duration(dto.getDuration())
                .status(dto.getStatus())
                .ageRating(dto.getAge_rating())
                .rating(dto.getRating())
                .country(dto.getCountry())
                .type(ContentType.valueOf(dto.getType().toUpperCase()))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .genresLinks(new ArrayList<>())
                .build();
    }

    private void uploadFilesToS3(ContentRequestDto dto, Content content) {
        String folderRoot = content.getType() == ContentType.MOVIE ? "films" : "serials";
        String folder = folderRoot + "/" + sanitizeFolderName(content.getTitle());

        if (dto.getLogoFile() != null && !dto.getLogoFile().isEmpty()) {
            FileValidator.validateFileExtension(dto.getLogoFile(), List.of("png", "jpg", "jpeg"));
            content.setLogoUrl(s3Service.uploadFile(dto.getLogoFile(), folder + "/logo/"));
        }

        if (content.getType() == ContentType.MOVIE && dto.getVideoFile() != null && !dto.getVideoFile().isEmpty()) {
            FileValidator.validateFileExtension(dto.getVideoFile(), List.of("mp4", "mkv", "avi"));
            content.setVideoUrl(s3Service.uploadFile(dto.getVideoFile(), folder + "/video/"));
        }
    }

    private void attachGenresToContent(Content content, List<Long> genreIds) {
        List<Genre> genres = genreRepository.findAllById(genreIds);
        for (Genre genre : genres) {
            GenresLink gl = new GenresLink();
            gl.setId(new ContentGenreKey(content.getId(), genre.getId()));
            gl.setContent(content);
            gl.setGenre(genre);
            content.getGenresLinks().add(gl);
        }
        contentRepository.save(content);
    }

    public ContentResponseDto updateContent(Long id, ContentRequestDto dto) {
        try {
            Optional<Content> existingOpt = contentRepository.findById(id);
            if (existingOpt.isEmpty()) return null;

            Content existing = existingOpt.get();
            existing.setKpId(dto.getKp_Id());
            existing.setTitle(dto.getTitle());
            existing.setDescription(dto.getDescription());
            existing.setShortDescription(dto.getShort_description());
            existing.setReleaseYear(dto.getReleaseYear());
            existing.setDuration(dto.getDuration());
            existing.setStatus(dto.getStatus());
            existing.setAgeRating(dto.getAge_rating());
            existing.setRating(dto.getRating());
            existing.setCountry(dto.getCountry());
            existing.setType(ContentType.valueOf(dto.getType().toUpperCase()));
            existing.setUpdatedAt(OffsetDateTime.now());

            String rootFolder = existing.getType() == ContentType.MOVIE ? "films" : "serials";
            String contentFolder = rootFolder + "/" + sanitizeFolderName(existing.getTitle());

            if (dto.getLogoFile() != null && !dto.getLogoFile().isEmpty()) {
                FileValidator.validateFileExtension(dto.getLogoFile(), List.of("png", "jpg", "jpeg", "gif"));
                existing.setLogoUrl(s3Service.uploadFile(dto.getLogoFile(), contentFolder + "/logo/"));
            }
            if (existing.getType() == ContentType.MOVIE && dto.getVideoFile() != null && !dto.getVideoFile().isEmpty()) {
                FileValidator.validateFileExtension(dto.getVideoFile(), List.of("mp4", "avi", "mkv"));
                existing.setVideoUrl(s3Service.uploadFile(dto.getVideoFile(), contentFolder + "/video/"));
            }

            existing.getGenresLinks().clear();
            List<Genre> genres = genreRepository.findAllById(dto.getGenreIds());
            List<GenresLink> genresLinks = genres.stream()
                    .map(genre -> {
                        GenresLink gl = new GenresLink();
                        gl.setId(new ContentGenreKey(existing.getId(), genre.getId()));
                        gl.setContent(existing);
                        gl.setGenre(genre);
                        return gl;
                    })
                    .collect(Collectors.toList());

            existing.setGenresLinks(genresLinks);
            return toResponseDto(contentRepository.save(existing));

        } catch (Exception e) {
            log.error("Error updating content: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating content: " + e.getMessage());
        }
    }

    public boolean deleteContent(Long id) {
        if (contentRepository.existsById(id)) {
            contentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<ContentResponseDto> getContentByGenres(List<String> genreNames) {
        if (genreNames == null || genreNames.isEmpty()) return List.of();

        return contentRepository.findByGenreNames(genreNames)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private ContentResponseDto toResponseDto(Content content) {
        List<String> genreNames = content.getGenresLinks().stream()
                .map(gl -> gl.getGenre().getName())
                .toList();

        Optional<KinopoiskApiService.Ratings> ratingsOpt =
                (content.getKpId() != null && content.getKpId() > 0)
                        ? Optional.ofNullable(kinopoiskApiService.getMovie(content.getKpId()))
                        .map(KinopoiskApiService.MovieApiResponse::getRating)
                        : Optional.empty();

        return ContentResponseDto.builder()
                .id(content.getId())
                .Kp_Id(content.getKpId())
                .title(content.getTitle())
                .description(content.getDescription())
                .short_description(content.getShortDescription())
                .releaseYear(content.getReleaseYear())
                .duration(content.getDuration())
                .type(content.getType().name())
                .status(content.getStatus())
                .age_rating(content.getAgeRating())
                .rating(content.getRating())
                .kp_rating(ratingsOpt.map(r -> r.getKp() != null ? r.getKp() : 0).orElse(0.0))
                .logo_url(content.getLogoUrl())
                .video_url(content.getVideoUrl())
                .country(content.getCountry())
                .createdAt(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt())
                .genres(genreNames)
                .build();
    }

    private String sanitizeFolderName(String title) {
        return title.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    }
}