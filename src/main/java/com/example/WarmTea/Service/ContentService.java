package com.example.WarmTea.Service;

import com.example.WarmTea.Dtos.ContentDto;
import com.example.WarmTea.Dtos.ContentDto.ContentRequestDto;
import com.example.WarmTea.Dtos.ContentDto.ContentResponseDto;
import com.example.WarmTea.Dtos.TrackDto;
import com.example.WarmTea.Models.*;
import com.example.WarmTea.Enums.ContentType;
import com.example.WarmTea.Repository.*;
import com.example.WarmTea.Utils.FileValidator;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final GenreRepository genreRepository;
    //private final KinopoiskApiService kinopoiskApiService;
    private final S3Service s3Service;
    private final UsersRepository usersRepository;
    private final FavoriteRepository favoriteRepository;
    private final RatingRepository ratingRepository;
    private final GenresLinkRepository genresLinkRepository; // <-- добавлено
    private final EntityManager entityManager;

    // === GET ALL CONTENT (полный список) ===
    public List<ContentResponseDto> getAllContent() {
        List<Content> contents = contentRepository.findAll();
        return mapContentsToResponseDto(contents);
    }

    // === GET ALL CONTENT SHORT (пагинированный) ===
    public Page<ContentDto.ShortContentDto> getAllContentShort(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Content> contentPage = contentRepository.findAll(pageable);

        List<Long> contentIds = contentPage.getContent().stream()
                .map(Content::getId)
                .collect(Collectors.toList());

        Map<Long, Long> votesCountMap = getVotesCountForContentIds(contentIds);

        return contentPage.map(content -> {
            Long votes = votesCountMap.getOrDefault(content.getId(), 0L);
            Double avgRating = content.getRating() != null ? content.getRating() : 0.0;

            return ContentDto.ShortContentDto.builder()
                    .id(content.getId())
                    .title(content.getTitle())
                    .logo_url(content.getLogoUrl())
                    .short_description(content.getShortDescription())
                    .releaseYear(content.getReleaseYear())
                    .type(content.getType().name())
                    .genres(content.getGenresLinks().stream()
                            .map(link -> link.getGenre().getName())
                            .collect(Collectors.toList()))
                    .averageRating(avgRating)
                    .votesCount(votes)
                    .build();
        });
    }

    // === GET CONTENT BY ID ===
    public ContentResponseDto getContentById(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Контент с ID " + id + " не найден"));

        Long votes = ratingRepository.getVotesCountForContent(id);
        votes = votes != null ? votes : 0L;

        ContentResponseDto dto = toResponseDto(content, votes);

        if (content.isSingleVideo()) {
            dto.setTracks(mapTracks(content.getMediaTracks()));
            dto.setVideoUrl(content.getVideoUrl());
        } else {
            List<ContentDto.EpisodeResponseDto> episodeDtos = content.getEpisodes().stream()
                    .map(episode -> ContentDto.EpisodeResponseDto.builder()
                            .id(episode.getId())
                            .seasonNumber(episode.getSeasonNumber())
                            .episodeNumber(episode.getEpisodeNumber())
                            .title(episode.getTitle())
                            .videoUrl(episode.getVideoUrl())
                            .duration(episode.getDuration())
                            .thumbnail(episode.getThumbnail())
                            .tracks(mapTracks(episode.getMediaTracks()))
                            .build())
                    .toList();
            dto.setSeasons(episodeDtos);
            dto.setEpisodes(episodeDtos);
            dto.setVideoUrl(null);
        }

        return dto;
    }

    // === ВСПОМОГАТЕЛЬНЫЙ МЕТОД ДЛЯ МАССОВОГО ПОЛУЧЕНИЯ КОЛИЧЕСТВА ГОЛОСОВ ===
    private Map<Long, Long> getVotesCountForContentIds(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Object[]> results = ratingRepository.getVotesCountForContentIds(contentIds);
        return results.stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    // === МАППИНГ СПИСКА КОНТЕНТА ===
    private List<ContentResponseDto> mapContentsToResponseDto(List<Content> contents) {
        if (contents == null || contents.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> contentIds = contents.stream().map(Content::getId).collect(Collectors.toList());
        Map<Long, Long> votesCountMap = getVotesCountForContentIds(contentIds);

        return contents.stream()
                .map(content -> {
                    Long votes = votesCountMap.getOrDefault(content.getId(), 0L);
                    return toResponseDto(content, votes);
                })
                .collect(Collectors.toList());
    }

    // === МАППИНГ ОДНОГО КОНТЕНТА ===
    private ContentResponseDto toResponseDto(Content content, Long votesCount) {
        List<String> genreNames = content.getGenresLinks().stream()
                .map(gl -> gl.getGenre().getName())
                .toList();
        List<Long> genreIds = content.getGenresLinks().stream()
                .map(gl -> gl.getGenre().getId())
                .collect(Collectors.toList());

        List<TrackDto> tracks = content.getMediaTracks() != null ?
                content.getMediaTracks().stream()
                        .map(t -> new TrackDto(t.getSrcUrl(), t.getLabel(), t.getLanguageCode(), "subtitles", t.isDefault()))
                        .toList() : Collections.emptyList();

//        Optional<KinopoiskApiService.Ratings> ratingsOpt =
//                (content.getKpId() != null && content.getKpId() > 0)
//                        ? Optional.ofNullable(kinopoiskApiService.getMovie(content.getKpId())).map(KinopoiskApiService.MovieApiResponse::getRating)
//                        : Optional.empty();

        Double avgRating = content.getRating() != null ? content.getRating() : 0.0;

        return ContentResponseDto.builder()
                .id(content.getId())
//                .Kp_Id(content.getKpId())
                .title(content.getTitle())
                .title_original(content.getTitle_original())
                .description(content.getDescription())
                .short_description(content.getShortDescription())
                .releaseYear(content.getReleaseYear())
                .duration(content.getDuration())
                .type(content.getType().name())
                .status(content.getStatus())
                .age_rating(content.getAgeRating())
                .averageRating(avgRating)
                .votesCount(votesCount != null ? votesCount : 0L)
//                .kp_rating(ratingsOpt.map(r -> r.getKp() != null ? r.getKp() : 0).orElse(0.0))
                .genres(genreNames)
                .genreIds(genreIds)
                .logo_url(content.getLogoUrl())
                .video_url(content.getVideoUrl())
                .country(content.getCountry())
                .createdAt(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt())
                .genres(genreNames)
                .tracks(tracks)
                .build();
    }

    // === ОСТАЛЬНЫЕ МЕТОДЫ ===

    @Transactional
    public ContentResponseDto createContent(ContentRequestDto dto) {
        try {
            Content content = buildContentEntity(dto);
            processMedia(dto, content);

            Content savedContent = contentRepository.save(content);

            if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
                attachGenresToContent(savedContent, dto.getGenreIds());
            }

            return toResponseDto(savedContent, 0L);
        } catch (Exception e) {
            log.error("Error creating content: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating content: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ContentResponseDto updateContent(Long id, ContentRequestDto dto) {
        try {
            Content existing = contentRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Контент не найден"));

//            existing.setKpId(dto.getKpId());
            existing.setTitle(dto.getTitle());
            existing.setTitle_original(dto.getTitle_original());
            existing.setDescription(dto.getDescription());
            existing.setShortDescription(dto.getShortDescription());
            existing.setReleaseYear(dto.getReleaseYear());
            existing.setDuration(dto.getDuration());
            existing.setStatus(dto.getStatus());
            existing.setAgeRating(dto.getAgeRating());
            existing.setCountry(dto.getCountry());
            existing.setType(ContentType.valueOf(dto.getType().toUpperCase()));
            existing.setUpdatedAt(OffsetDateTime.now());
            existing.setVideoUrl(dto.getVideoUrl());

            processMedia(dto, existing);

            // --- Обновление жанров ---
            genresLinkRepository.deleteByContentId(id);
            entityManager.flush(); // <-- добавляем эту строку
            existing.getGenresLinks().clear();

            if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
                attachGenresToContent(existing, dto.getGenreIds());
            }

            Content updated = contentRepository.save(existing);
            Long votes = ratingRepository.getVotesCountForContent(id);
            votes = votes != null ? votes : 0L;
            return toResponseDto(updated, votes);
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
        List<Content> contents = contentRepository.findByGenreNames(genreNames);
        return mapContentsToResponseDto(contents);
    }

    @Transactional
    public void addToFavorites(Long userId, Long contentId) {
        User user = usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Content content = contentRepository.findById(contentId).orElseThrow(() -> new RuntimeException("Контент не найден"));
        if (!favoriteRepository.existsByUserAndContent(user, content)) {
            favoriteRepository.save(Favorite.builder().user(user).content(content).build());
        }
    }

    @Transactional
    public void removeFromFavorites(Long userId, Long contentId) {
        User user = usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Content content = contentRepository.findById(contentId).orElseThrow(() -> new RuntimeException("Контент не найден"));
        favoriteRepository.deleteByUserAndContent(user, content);
    }

    @Transactional(readOnly = true)
    public List<Long> getFavoriteIds(Long userId) {
        User user = usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return favoriteRepository.findByUser(user).stream().map(f -> f.getContent().getId()).toList();
    }

    @Transactional(readOnly = true)
    public List<ContentDto.ShortContentDto> getFullFavorites(Long userId) {
        User user = usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        List<Content> favoriteContents = favoriteRepository.findByUser(user).stream()
                .map(Favorite::getContent)
                .collect(Collectors.toList());

        List<Long> contentIds = favoriteContents.stream().map(Content::getId).collect(Collectors.toList());
        Map<Long, Long> votesCountMap = getVotesCountForContentIds(contentIds);

        return favoriteContents.stream().map(content -> {
            Long votes = votesCountMap.getOrDefault(content.getId(), 0L);
            Double avgRating = content.getRating() != null ? content.getRating() : 0.0;

            return ContentDto.ShortContentDto.builder()
                    .id(content.getId())
                    .title(content.getTitle())
                    .logo_url(content.getLogoUrl())
                    .short_description(content.getShortDescription())
                    .releaseYear(content.getReleaseYear())
                    .type(content.getType().name())
                    .averageRating(avgRating)
                    .votesCount(votes)
                    .build();
        }).toList();
    }

    public List<ContentDto.ShortContentDto> getRandomContentByType(String type, int limit) {
        ContentType contentType = ContentType.valueOf(type.toUpperCase());
        List<Content> contents = contentRepository.findRandomByType(contentType.name(), limit);

        if (contents.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> contentIds = contents.stream().map(Content::getId).collect(Collectors.toList());
        Map<Long, Long> votesCountMap = getVotesCountForContentIds(contentIds);

        return contents.stream().map(content -> {
            Long votes = votesCountMap.getOrDefault(content.getId(), 0L);
            Double avgRating = content.getRating() != null ? content.getRating() : 0.0;

            return ContentDto.ShortContentDto.builder()
                    .id(content.getId())
                    .title(content.getTitle())
                    .logo_url(content.getLogoUrl())
                    .short_description(content.getShortDescription())
                    .releaseYear(content.getReleaseYear())
                    .type(content.getType().name())
                    .genres(content.getGenresLinks().stream()
                            .map(link -> link.getGenre().getName())
                            .collect(Collectors.toList()))
                    .averageRating(avgRating)
                    .votesCount(votes)
                    .build();
        }).collect(Collectors.toList());
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private Content buildContentEntity(ContentRequestDto dto) {
        return Content.builder()
//                .kpId(dto.getKpId())
                .title(dto.getTitle())
                .title_original(dto.getTitle_original())
                .description(dto.getDescription())
                .shortDescription(dto.getShortDescription())
                .releaseYear(dto.getReleaseYear())
                .duration(dto.getDuration())
                .status(dto.getStatus())
                .ageRating(dto.getAgeRating())
                .country(dto.getCountry())
                .videoUrl(dto.getVideoUrl())
                .type(ContentType.valueOf(dto.getType().toUpperCase().trim()))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .genresLinks(new ArrayList<>())
                .mediaTracks(new ArrayList<>())
                .build();
    }

    private void processMedia(ContentRequestDto dto, Content content) {
        String folderRoot = (content.getType() == ContentType.MOVIE || content.getType() == ContentType.ANIME)
                ? "films" : "serials";
        String folder = folderRoot + "/" + sanitizeFolderName(content.getTitle());

        if (dto.getLogoFile() != null && !dto.getLogoFile().isEmpty()) {
            FileValidator.validateFileExtension(dto.getLogoFile(), List.of("png", "jpg", "jpeg"));
            content.setLogoUrl(s3Service.uploadFile(dto.getLogoFile(), folder + "/logo/"));
        }

        if (dto.getVideoUrl() != null && !dto.getVideoUrl().isBlank()) {
            content.setVideoUrl(dto.getVideoUrl());
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
    }

    @Transactional
    public void removeGenreFromContent(Long contentId, Long genreId) {
        if (!contentRepository.existsById(contentId)) {
            throw new NoSuchElementException("Контент не найден");
        }
        if (!genreRepository.existsById(genreId)) {
            throw new NoSuchElementException("Жанр не найден");
        }
        genresLinkRepository.deleteByContentIdAndGenreId(contentId, genreId);
    }

    private List<TrackDto> mapTracks(List<MediaTrack> mediaTracks) {
        if (mediaTracks == null) return Collections.emptyList();
        return mediaTracks.stream()
                .map(t -> new TrackDto(t.getSrcUrl(), t.getLabel(), t.getLanguageCode(), "subtitles", t.isDefault()))
                .toList();
    }

    public List<ContentDto.ShortContentDto> getContentShortByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return contentRepository.findAllById(ids).stream()
                .map(content -> {
                    // Получаем количество голосов
                    Long votes = ratingRepository.getVotesCountForContent(content.getId());
                    votes = votes != null ? votes : 0L;
                    Double avgRating = content.getRating() != null ? content.getRating() : 0.0;

                    return ContentDto.ShortContentDto.builder()
                            .id(content.getId())
                            .title(content.getTitle())
                            .logo_url(content.getLogoUrl())
                            .short_description(content.getShortDescription())
                            .releaseYear(content.getReleaseYear())
                            .type(content.getType().name())
                            .genres(content.getGenresLinks().stream()
                                    .map(link -> link.getGenre().getName())
                                    .collect(Collectors.toList()))
                            .averageRating(avgRating)
                            .votesCount(votes)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String sanitizeFolderName(String title) {
        return title.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    }
}