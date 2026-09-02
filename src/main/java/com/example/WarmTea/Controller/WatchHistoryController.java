package com.example.WarmTea.Controller;

import com.example.WarmTea.Dtos.WatchHistoryRequestDto;
import com.example.WarmTea.Dtos.WatchHistoryResponseDto;
import com.example.WarmTea.Service.WatchHistoryService;
import com.example.WarmTea.Utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watch-history")
@RequiredArgsConstructor
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;
    private final JwtUtils jwtUtils;

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Отсутствует или некорректный заголовок Authorization");
        }
        String token = authHeader.substring(7);
        return jwtUtils.extractUserId(token);
    }

    @PostMapping
    @Operation(summary = "Сохранить или обновить прогресс просмотра")
    public ResponseEntity<WatchHistoryResponseDto> saveProgress(
            @RequestBody WatchHistoryRequestDto request,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(watchHistoryService.saveOrUpdate(userId, request));
    }

    @GetMapping
    @Operation(summary = "Получить сохранённый прогресс для контента/эпизода")
    public ResponseEntity<WatchHistoryResponseDto> getProgress(
            @RequestParam Long contentId,
            @RequestParam(required = false) Long episodeId,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = getUserIdFromToken(authHeader);
        WatchHistoryResponseDto progress = watchHistoryService.getProgress(userId, contentId, episodeId);
        return ResponseEntity.ok(progress);
    }

    // WatchHistoryController.java
    @GetMapping("/user")
    @Operation(summary = "Получить историю просмотров текущего пользователя")
    public ResponseEntity<List<WatchHistoryResponseDto>> getUserHistory(
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(watchHistoryService.getUserHistory(userId));
    }

    @GetMapping("/last")
    @Operation(summary = "Получить последний прогресс для контента (без эпизода)")
    public ResponseEntity<WatchHistoryResponseDto> getLastProgressForContent(
            @RequestParam Long contentId,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = getUserIdFromToken(authHeader);
        WatchHistoryResponseDto progress = watchHistoryService.getLastProgressForContent(userId, contentId);
        return ResponseEntity.ok(progress);
    }
}