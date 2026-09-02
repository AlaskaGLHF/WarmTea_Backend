package com.example.WarmTea.Service;

import com.example.WarmTea.Dtos.AverageRatingResponseDto;
import com.example.WarmTea.Dtos.RatingRequestDto;
import com.example.WarmTea.Dtos.RatingResponseDto;
import com.example.WarmTea.Models.Content;
import com.example.WarmTea.Models.Rating;
import com.example.WarmTea.Models.User;
import com.example.WarmTea.Repository.ContentRepository;
import com.example.WarmTea.Repository.RatingRepository;
import com.example.WarmTea.Repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UsersRepository usersRepository;
    private final ContentRepository contentRepository;

    // === СОЗДАНИЕ / ОБНОВЛЕНИЕ ОЦЕНКИ ===
    @Transactional
    public RatingResponseDto rateContent(Long userId, RatingRequestDto request) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Content content = contentRepository.findById(request.getContentId())
                .orElseThrow(() -> new RuntimeException("Контент не найден"));

        Rating existing = ratingRepository.findByUserIdAndContentId(userId, request.getContentId())
                .orElse(null);

        Rating rating;
        if (existing != null) {
            existing.setScore(request.getScore());
            existing.setCreatedAt(OffsetDateTime.now());
            rating = ratingRepository.save(existing);
            log.info("Оценка обновлена: пользователь {} контент {} оценка {}", userId, request.getContentId(), request.getScore());
        } else {
            rating = Rating.builder()
                    .user(user)
                    .content(content)
                    .score(request.getScore())
                    .createdAt(OffsetDateTime.now())
                    .build();
            rating = ratingRepository.save(rating);
            log.info("Оценка создана: пользователь {} контент {} оценка {}", userId, request.getContentId(), request.getScore());
        }

        // Обновляем агрегированный рейтинг в таблице content
        updateContentAverageRating(content.getId());

        return mapToResponseDto(rating);
    }

    // === ПОЛУЧИТЬ ВСЕ ОЦЕНКИ ПОЛЬЗОВАТЕЛЯ ===
    @Transactional(readOnly = true)
    public List<RatingResponseDto> getUserRatings(Long userId) {
        if (!usersRepository.existsById(userId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        return ratingRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    // === ПОЛУЧИТЬ ОЦЕНКУ КОНКРЕТНОГО ПОЛЬЗОВАТЕЛЯ ДЛЯ КОНКРЕТНОГО КОНТЕНТА ===
    @Transactional(readOnly = true)
    public RatingResponseDto getUserRatingForContent(Long userId, Long contentId) {
        return ratingRepository.findByUserIdAndContentId(userId, contentId)
                .map(this::mapToResponseDto)
                .orElse(null);
    }

    // === ПОЛУЧИТЬ СРЕДНИЙ РЕЙТИНГ И КОЛИЧЕСТВО ГОЛОСОВ ДЛЯ КОНТЕНТА ===
    @Transactional(readOnly = true)
    public AverageRatingResponseDto getContentRating(Long contentId) {
        if (!contentRepository.existsById(contentId)) {
            throw new RuntimeException("Контент не найден");
        }
        Double avg = ratingRepository.getAverageScoreForContent(contentId);
        Long count = ratingRepository.getVotesCountForContent(contentId);
        return AverageRatingResponseDto.builder()
                .contentId(contentId)
                .averageScore(avg != null ? avg : 0.0)
                .votesCount(count != null ? count : 0L)
                .build();
    }

    // === ПОЛУЧИТЬ РЕЙТИНГИ ДЛЯ СПИСКА КОНТЕНТОВ (МАССОВО) ===
    @Transactional(readOnly = true)
    public Map<Long, AverageRatingResponseDto> getContentRatingsForContentIds(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<AverageRatingResponseDto> stats = ratingRepository.getAverageScoresForContentIds(contentIds);
        return stats.stream()
                .collect(Collectors.toMap(
                        AverageRatingResponseDto::getContentId,
                        dto -> dto
                ));
    }

    // === УДАЛИТЬ ОЦЕНКУ ===
    @Transactional
    public void deleteRating(Long userId, Long contentId) {
        Rating rating = ratingRepository.findByUserIdAndContentId(userId, contentId)
                .orElseThrow(() -> new RuntimeException("Оценка не найдена"));
        ratingRepository.delete(rating);
        updateContentAverageRating(contentId);
        log.info("Оценка удалена: пользователь {} контент {}", userId, contentId);
    }

    // === ВСПОМОГАТЕЛЬНЫЙ МЕТОД ДЛЯ ОБНОВЛЕНИЯ АГРЕГИРОВАННОГО РЕЙТИНГА В CONTENT ===
    private void updateContentAverageRating(Long contentId) {
        Double avg = ratingRepository.getAverageScoreForContent(contentId);
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Контент не найден"));
        content.setRating(avg != null ? avg : 0.0);
        contentRepository.save(content);
    }

    // === МАППИНГ ===
    private RatingResponseDto mapToResponseDto(Rating rating) {
        return RatingResponseDto.builder()
                .id(rating.getId())
                .userId(rating.getUser().getId())
                .username(rating.getUser().getUsername())
                .contentId(rating.getContent().getId())
                .contentTitle(rating.getContent().getTitle())
                .score(rating.getScore())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}