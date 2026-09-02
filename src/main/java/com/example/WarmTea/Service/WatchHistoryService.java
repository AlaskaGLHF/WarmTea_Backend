package com.example.WarmTea.Service;

import com.example.WarmTea.Dtos.WatchHistoryRequestDto;
import com.example.WarmTea.Dtos.WatchHistoryResponseDto;
import com.example.WarmTea.Models.Content;
import com.example.WarmTea.Models.Episode;
import com.example.WarmTea.Models.User;
import com.example.WarmTea.Models.WatchHistory;
import com.example.WarmTea.Repository.ContentRepository;
import com.example.WarmTea.Repository.EpisodeRepository;
import com.example.WarmTea.Repository.UsersRepository;
import com.example.WarmTea.Repository.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WatchHistoryService {

    private final WatchHistoryRepository watchHistoryRepository;
    private final UsersRepository usersRepository;
    private final ContentRepository contentRepository;
    private final EpisodeRepository episodeRepository;

    @Transactional
    public WatchHistoryResponseDto saveOrUpdate(Long userId, WatchHistoryRequestDto request) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Content content = contentRepository.findById(request.getContentId())
                .orElseThrow(() -> new RuntimeException("Контент не найден"));

        Episode episode = null;
        if (request.getEpisodeId() != null) {
            episode = episodeRepository.findById(request.getEpisodeId())
                    .orElseThrow(() -> new RuntimeException("Эпизод не найден"));
        }

        WatchHistory existing = watchHistoryRepository
                .findByUserIdAndContentIdAndEpisodeId(userId, request.getContentId(), request.getEpisodeId())
                .orElse(null);

        WatchHistory history;
        if (existing != null) {
            existing.setStoppedAt(request.getStoppedAt());
            existing.setWatchedAt(OffsetDateTime.now());
            history = watchHistoryRepository.save(existing);
            log.info("Обновлена история просмотра: user {} content {} episode {} stoppedAt {}",
                    userId, request.getContentId(), request.getEpisodeId(), request.getStoppedAt());
        } else {
            history = WatchHistory.builder()
                    .user(user)
                    .content(content)
                    .episode(episode)
                    .stoppedAt(request.getStoppedAt())
                    .watchedAt(OffsetDateTime.now())
                    .build();
            history = watchHistoryRepository.save(history);
            log.info("Создана история просмотра: user {} content {} episode {} stoppedAt {}",
                    userId, request.getContentId(), request.getEpisodeId(), request.getStoppedAt());
        }

        return toDto(history);
    }

    @Transactional(readOnly = true)
    public WatchHistoryResponseDto getProgress(Long userId, Long contentId, Long episodeId) {
        WatchHistory history = watchHistoryRepository
                .findByUserIdAndContentIdAndEpisodeId(userId, contentId, episodeId)
                .orElse(null);
        return history != null ? toDto(history) : null;
    }

    @Transactional(readOnly = true)
    public List<WatchHistoryResponseDto> getUserHistory(Long userId) {
        if (!usersRepository.existsById(userId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        List<WatchHistory> histories = watchHistoryRepository.findByUserIdOrderByWatchedAtDesc(userId);
        return histories.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WatchHistoryResponseDto getLastProgressForContent(Long userId, Long contentId) {
        List<WatchHistory> histories = watchHistoryRepository
                .findByUserIdAndContentIdOrderByWatchedAtDesc(userId, contentId);
        if (histories.isEmpty()) {
            return null;
        }
        return toDto(histories.get(0));
    }

    // Единый метод маппинга (удалите старую версию)
    private WatchHistoryResponseDto toDto(WatchHistory history) {
        Episode episode = history.getEpisode();
        return WatchHistoryResponseDto.builder()
                .id(history.getId())
                .userId(history.getUser().getId())
                .contentId(history.getContent().getId())
                .contentTitle(history.getContent().getTitle())
                .contentLogoUrl(history.getContent().getLogoUrl())
                .contentType(history.getContent().getType().name())
                .contentReleaseYear(history.getContent().getReleaseYear())
                .episodeId(episode != null ? episode.getId() : null)
                .episodeTitle(episode != null ? episode.getTitle() : null)
                .seasonNumber(episode != null ? episode.getSeasonNumber() : null)
                .episodeNumber(episode != null ? episode.getEpisodeNumber() : null)
                .stoppedAt(history.getStoppedAt())
                .duration(episode != null ? episode.getDuration() : history.getContent().getDuration())
                .watchedAt(history.getWatchedAt())
                .build();
    }
}