package com.example.WarmTea.Controller;

import com.example.WarmTea.Dtos.AverageRatingResponseDto;
import com.example.WarmTea.Dtos.RatingRequestDto;
import com.example.WarmTea.Dtos.RatingResponseDto;
import com.example.WarmTea.Service.RatingService;
import com.example.WarmTea.Utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Slf4j
public class RatingController {

    private final RatingService ratingService;
    private final JwtUtils jwtUtils;

    private Long getUserIdFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Отсутствует или некорректный заголовок Authorization");
        }
        String token = authHeader.substring(7);
        return jwtUtils.extractUserId(token);
    }

    @PostMapping
    @Operation(summary = "Поставить или обновить оценку")
    @ApiResponse(responseCode = "200", description = "Оценка сохранена")
    public ResponseEntity<RatingResponseDto> rateContent(
            @Valid @RequestBody RatingRequestDto request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = getUserIdFromAuthHeader(authHeader);
        log.info("Пользователь {} ставит оценку контенту {} = {}", userId, request.getContentId(), request.getScore());
        return ResponseEntity.ok(ratingService.rateContent(userId, request));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить все оценки пользователя")
    public ResponseEntity<List<RatingResponseDto>> getUserRatings(
            @Parameter(description = "ID пользователя") @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ratingService.getUserRatings(userId));
    }

    @GetMapping("/content/{contentId}")
    @Operation(summary = "Получить средний рейтинг контента и количество голосов")
    public ResponseEntity<AverageRatingResponseDto> getContentRating(
            @Parameter(description = "ID контента") @PathVariable Long contentId
    ) {
        return ResponseEntity.ok(ratingService.getContentRating(contentId));
    }

    @DeleteMapping
    @Operation(summary = "Удалить свою оценку для контента")
    @ApiResponse(responseCode = "204", description = "Оценка удалена")
    public ResponseEntity<Void> deleteRating(
            @RequestParam Long contentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = getUserIdFromAuthHeader(authHeader);
        log.info("Пользователь {} удаляет оценку для контента {}", userId, contentId);
        ratingService.deleteRating(userId, contentId);
        return ResponseEntity.noContent().build();
    }
}